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
	W         = 400
	Center    = W / 2
	Tolerance = 0
	R2        = (Center - Tolerance) * (Center - Tolerance)
)

var LCHSat = StepConfig{num: 20, step: 0.05, init: 0}
var HSVSat = StepConfig{num: 20, step: 0.05, init: 0}
var UnrealSat = StepConfig{num: 50, step: 0.02, init: 0}
var UnrealBright = StepConfig{num: 100, step: 0.01, init: 1.00}

func saturateLchDisc(sat float64) VariableFunc {
	return func(arg float64) (ParamFunc, string) {
		return func (theta, _ float64) colorful.Color {
			return colorful.Hcl(theta, sat, arg).Clamped()
		}, fmt.Sprintf("level=%.2f-sat=%.2f", arg, sat)
	}
}

func saturateHsvDisc(sat float64) VariableFunc {
	return func(arg float64) (ParamFunc, string) {
		return func (theta, _ float64) colorful.Color {
			return colorful.Hsv(theta, sat, arg).Clamped()
		}, fmt.Sprintf("level=%.2f-sat=%.2f", arg, sat)
	}
}

func saturateUnrealDisc(sat float64) VariableFunc {
	return func(arg float64) (ParamFunc, string) {
		return func (theta, _ float64) colorful.Color {
			return colorful.Hcl(theta, sat, arg)
		}, fmt.Sprintf("level=%.2f-sat=%.2f", arg, sat)
	}
}

func brightnessUnrealDisc(level float64) VariableFunc {
	return func(arg float64) (ParamFunc, string) {
		return func (theta, _ float64) colorful.Color {
			return colorful.Hcl(theta, arg, level)
		}, fmt.Sprintf("level=%.2f-sat=%.2f", level, arg)
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
}

func (cfg StepConfig) writeDiscTextures(pfx string, vfunc VariableFunc) {
	for i := 0; i <= cfg.num; i++ {
		level := cfg.init + float64(i) * cfg.step
		pfunc, desc := vfunc(level)
		texture := cfg.makeDiscTexture(level, pfunc)
		cfg.writeTexture(pfx, "disc", desc, level, texture)
	}
}

func (cfg StepConfig) writeTexture(pfx, kind, desc string, level float64, img image.Image) {
	f, err := os.Create(fmt.Sprintf("%s-%s-%s.png", pfx, kind, desc))
	if err != nil {
		panic(err)
	}
	defer f.Close()
	if err := png.Encode(f, img); err != nil {
		panic(err)
	}
}

func (cfg StepConfig) makeDiscTexture(level float64, pfunc ParamFunc) image.Image {
	imgLCh := image.NewRGBA(image.Rect(0, 0, W, W))

	for xi := 0; xi < W; xi++ {
		xc := float64(xi) + 0.5
		xd := Center - xc
		xd2 := xd * xd

		for yi := 0; yi < W; yi++ {
			yc := float64(yi) + 0.5
			yd := Center - yc
			yd2 := yd * yd
			xy2 := xd2+yd2

			if xy2 > R2 {
				imgLCh.Set(xi, yi, color.RGBA{})
				continue
			}

			theta := math.Atan(yd/xd) + (math.Pi / 2)

			if xd < 0 {
				theta += math.Pi
			}

			const RadToDeg = 180 / math.Pi

			imgLCh.Set(xi, yi, pfunc(theta*RadToDeg, math.Sqrt(xy2)))
		}
	}

	return imgLCh
}
