package main

import (
	"fmt"
	"image"
	"image/color"
	"image/png"
	"math"
	"os"

	colorful "github.com/lucasb-eyer/go-colorful"
)

type (
	Cfunc func(theta, chroma, level float64) colorful.Color
)

const (
	W         = 600
	Center    = W / 2
	Tolerance = 10
	R2        = (Center - Tolerance) * (Center - Tolerance)
)

func main() {
	writeTextures("lch", colorful.Hcl)
	writeTextures("hsv", colorful.Hsv)
}

func writeTextures(pfx string, cfunc Cfunc) {
	const (
		num    = 10
		factor = 0.10
	)
	for i := 1; i <= num; i++ {
		l := float64(i) * factor
		writeTexture(pfx, l, makeTexture(l, cfunc))
	}
}

func writeTexture(pfx string, level float64, img image.Image) {
	f, err := os.Create(fmt.Sprintf("%s-disc-%.2f.png", pfx, level))
	if err != nil {
		panic(err)
	}
	defer f.Close()
	if err := png.Encode(f, img); err != nil {
		panic(err)
	}
}

func makeTexture(level float64, cfunc Cfunc) image.Image {
	imgLCh := image.NewRGBA(image.Rect(0, 0, W, W))

	for xi := 0; xi < W; xi++ {
		xc := float64(xi) + 0.5
		xd := Center - xc
		xd2 := xd * xd

		for yi := 0; yi < W; yi++ {
			yc := float64(yi) + 0.5
			yd := Center - yc
			yd2 := yd * yd

			if xd2+yd2 > R2 {
				imgLCh.Set(xi, yi, color.RGBA{})
				continue
			}

			theta := math.Atan(yd/xd) + (math.Pi / 2)

			if xd < 0 {
				theta += math.Pi
			}

			chroma := 1.

			const RadToDeg = 180 / math.Pi

			imgLCh.Set(xi, yi, cfunc(theta*RadToDeg, chroma, level).Clamped())
		}
	}

	return imgLCh
}
