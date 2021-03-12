#!/bin/bash

#
# Processes videos for display on the Rainbow Bridge.  
#
# The input video should be larger than 420x30.  This script will
# produce a 420x30 video.  It will first scale the video to a width
# of 420 pixels.  It will then crop out a 30 pixels high strip.  By 
# default it will crop the center of the video.  The Y coordinate of 
# the top left of the crop can be specified via -y 30 for example.
# Currently, it doesn't make sense to specify an X coordinate 
# crop starting location because we always scale down to 420 wide.
#
# I'm using ffmpeg 4.3.1 on Mac OS X.
# My older version of ffmpeg 2.8.5 I had previously installed didn't
# work for this workflow.
# 
# Also note, for portrait orientation videos from mobile phones, the
# pixel data is still in landscape orientation and the file just
# has a rotation metadata tag set.  In order to extract a 420x30
# video section out of a mobile phone video, you first need to
# remove the rotation tag which is quick.  And then you need to
# transpose the video aka rotate the pixels which will re-encode
# all the frames.
#
# ffmpeg -i in.mp4 -map_metadata 0 -metadata:s:v rotate="0" -codec copy out.mp4
# ffmpeg -i out.mp4 -vf "transpose=1" out2.mp4
#
# https://stackoverflow.com/questions/39788972/ffmpeg-override-output-file-if-exists
#

CROP_X=-1
CROP_Y=-1
while getopts 'x:y:' OPTION
do
	case $OPTION in
		x) CROP_X="$OPTARG"
		   echo "CROP_X: $CROP_X"
		;;
		y) CROP_Y="$OPTARG"
		   echo "CROP_Y: $CROP_Y"
		;;
	esac
done
shift $(($OPTIND -1))

FILE=$1
echo "Processing $1"


TMP="videotmp.mp4"
OUT=$2
echo "Saving result to $OUT"

OUT_WIDTH=420
OUT_HEIGHT=30

# Get the size of input video:
eval $(ffprobe -v error -of flat=s=_ -select_streams v:0 -show_entries stream=height,width ${FILE})
IN_WIDTH=${streams_stream_0_width}
IN_HEIGHT=${streams_stream_0_height}

echo "$IN_WIDTH x $IN_HEIGHT"
SCALE=`echo "$OUT_WIDTH / $IN_WIDTH" | bc -l`
echo "Scale: $SCALE"

# Get the difference between actual and desired size
W_DIFF=$[ ${OUT_WIDTH} - ${IN_WIDTH} ]
H_DIFF=$[ ${OUT_HEIGHT} - ${IN_HEIGHT} ]

echo "W_DIFF, H_DIFF: $W_DIFF $H_DIFF"

# We always want to scale to the width and then crop the height of the video.
SCALE="${OUT_WIDTH}:-2"
CROP_SIDE="h"

# Then perform a first resizing
ffmpeg -y -i ${FILE} -vf scale=${SCALE} -video_track_timescale 30 -crf 15 ${TMP}

# Now get the temporary video size
eval $(ffprobe -v error -of flat=s=_ -select_streams v:0 -show_entries stream=height,width ${TMP})
IN_WIDTH=${streams_stream_0_width}
IN_HEIGHT=${streams_stream_0_height}

echo "--------------------------------------------"
echo "IN_WIDTH x IN_HEIGHT: $IN_WIDTH x $IN_HEIGHT"

# Calculate how much we should crop
if [ "z${CROP_SIDE}" = "zh" ] ; then
  DIFF=$[ ${IN_HEIGHT} - ${OUT_HEIGHT} ]
  if [ $CROP_Y -eq -1 ] ; then
	let "CROP_Y = $DIFF / 2"
  fi
  # We have already scaled to the appropriate width so always crop zero of the width.
  CROP_X=0
  CROP="in_w:in_h-${DIFF}:$CROP_X:$CROP_Y"
elif [ "z${CROP_SIDE}" = "zw" ] ; then
  DIFF=$[ ${IN_WIDTH} - ${OUT_WIDTH} ]
  if [ $CROP_X -eq -1 ] ; then
  	let "CROP_X = $DIFF / 2"
  fi
  CROP="in_w-${DIFF}:in_h:$CROP_X:$CROP_Y"
fi

echo "ffmpeg crop argument: $CROP"

# Then crop...
ffmpeg -i ${TMP} -filter:v "crop=${CROP}" -crf 15 ${OUT}
