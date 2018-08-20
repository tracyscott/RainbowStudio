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
	// ParamFunc takes either polar (theta, radius) or cartesian
	// coordinates (x, y).  Theta is 0...2*PI, others are 0...1.
	ParamFunc func(p1, p2 float64) colorful.Color
	VariableFunc func(v float64) (ParamFunc, string)
	ColorFunc func(colorful.Color) colorful.Color

	StepConfig struct {
		num  int
		init float64
		step float64
	}
)

const (
	// Used for discs, wheels, etc
	RADIUS         = 400
	RSQARED        = (Center - Tolerance) * (Center - Tolerance)
	Center    = RADIUS / 2
	Tolerance = 0

	// Used for square lookup tables
	WIDTH = 1000
)

var LCHSat = StepConfig{num: 20, step: 0.05, init: 0}
var HSVSat = StepConfig{num: 20, step: 0.05, init: 0}
var UnrealBright = StepConfig{num: 40, step: 0.01, init: 1.3}

// Polar LCH: saturation variable, level range of values
func saturateLchDisc(sat float64) VariableFunc {
	return func(arg float64) (ParamFunc, string) {
		return func (theta, _ float64) colorful.Color {
			return colorful.Hcl(theta, sat, arg).Clamped()
		}, fmt.Sprintf("level=%.2f-sat=%.2f", arg, sat)
	}
}

// Polar HSV: saturation variable, level range of values
func saturateHsvDisc(sat float64) VariableFunc {
	return func(arg float64) (ParamFunc, string) {
		return func (theta, _ float64) colorful.Color {
			return colorful.Hsv(theta, sat, arg).Clamped()
		}, fmt.Sprintf("level=%.2f-sat=%.2f", arg, sat)
	}
}

// Polar LCH: level variable, saturation range of values
func brightnessUnrealDisc(level float64) VariableFunc {
	return func(arg float64) (ParamFunc, string) {
		return func (theta, _ float64) colorful.Color {
			return colorful.Hcl(theta, arg, level)
		}, fmt.Sprintf("level=%.2f-sat=%.2f", level, arg)
	}
}

func (cfg StepConfig) writeDiscTextures(pfx string, vfunc VariableFunc) {
	for i := 0; i <= cfg.num; i++ {
		level := cfg.init + float64(i) * cfg.step
		pfunc, desc := vfunc(level)
		texture := cfg.makeDiscTexture(pfunc)
		writeTexture(pfx, "disc", desc, texture)
	}
}

// Polar coordinates
func (cfg StepConfig) makeDiscTexture(pfunc ParamFunc) image.Image {
	imgPolar := image.NewRGBA(image.Rect(0, 0, RADIUS, RADIUS))

	for xi := 0; xi < RADIUS; xi++ {
		xc := float64(xi) + 0.5
		xd := Center - xc
		xd2 := xd * xd

		for yi := 0; yi < RADIUS; yi++ {
			yc := float64(yi) + 0.5
			yd := Center - yc
			yd2 := yd * yd
			xy2 := xd2+yd2

			if xy2 > RSQARED {
				imgPolar.Set(xi, yi, color.RGBA{})
				continue
			}

			theta := math.Atan(yd/xd) + (math.Pi / 2)

			if xd < 0 {
				theta += math.Pi
			}

			const RadToDeg = 180 / math.Pi

			imgPolar.Set(xi, yi, pfunc(theta*RadToDeg, math.Sqrt(xy2)))
		}
	}

	return imgPolar
}

// Rectaungular coordinates
func makeSquareTexture(pfunc ParamFunc) image.Image {
	imgSquare := image.NewRGBA(image.Rect(0, 0, WIDTH, WIDTH))

	for xi := 0; xi < WIDTH; xi++ {
		for yi := 0; yi < WIDTH; yi++ {
			imgSquare.Set(xi, yi, pfunc(float64(xi) / WIDTH, float64(yi) / WIDTH));
		}
	}

	return imgSquare
}

func writeTexture(pfx, kind, desc string, img image.Image) {
	f, err := os.Create(fmt.Sprintf("%s-%s-%s.png", pfx, kind, desc))
	if err != nil {
		panic(err)
	}
	defer f.Close()
	if err := png.Encode(f, img); err != nil {
		panic(err)
	}
}

func main() {
	// Fully saturated, real-color discs
	LCHSat.writeDiscTextures("lch", saturateLchDisc(1))
	HSVSat.writeDiscTextures("hsv", saturateHsvDisc(1))

	// Over saturated/bright unreal colors
	for level := 1.02; level <= 1.3; level += .07 {
		UnrealBright.writeDiscTextures("unreal", brightnessUnrealDisc(level))
	}

	// From eye-balling on an OS X screen which means not very
	// much, this is the place where you see all the colors as
	// brightly as they get w/o the appearance of white or black.
	const bestLevel = 0.6

	// LAB lookup table (BackgroundPulse uses the perimeter of this image)
	writeTexture("lab", "square", "lookup",
		makeSquareTexture(func (x, y float64) colorful.Color {
			a := (x - 0.5) * 2
			b := (y - 0.5) * 2
		return colorful.Lab(bestLevel, a, b).Clamped()
	}))
}
