Add-Type -AssemblyName System.Drawing

$OutDir = Join-Path $PSScriptRoot "..\src\main\resources\assets\huntercraft\textures\gui\abilities"

function Color($hex) {
    return [System.Drawing.ColorTranslator]::FromHtml($hex)
}

function Brush($hex) {
    return [System.Drawing.SolidBrush]::new((Color $hex))
}

function Pen($hex, $width) {
    $pen = [System.Drawing.Pen]::new((Color $hex), $width)
    $pen.StartCap = [System.Drawing.Drawing2D.LineCap]::Square
    $pen.EndCap = [System.Drawing.Drawing2D.LineCap]::Square
    $pen.LineJoin = [System.Drawing.Drawing2D.LineJoin]::Miter
    return $pen
}

function Draw-Arc($g, $rect, $start, $sweep, $color, $width) {
    $g.DrawArc((Pen "#101010" ($width + 2)), $rect, $start, $sweep)
    $g.DrawArc((Pen $color $width), $rect, $start, $sweep)
}

function Draw-Person($g, $x, $y, $fill) {
    $outline = Brush "#151515"
    $body = Brush $fill
    $g.FillEllipse($outline, $x + 9, $y, 14, 14)
    $g.FillEllipse($body, $x + 11, $y + 2, 10, 10)
    $g.FillRectangle($outline, $x + 10, $y + 14, 12, 22)
    $g.FillRectangle($body, $x + 13, $y + 16, 6, 18)
    $g.FillRectangle($outline, $x + 5, $y + 18, 5, 18)
    $g.FillRectangle($outline, $x + 22, $y + 18, 5, 18)
    $g.FillRectangle($outline, $x + 9, $y + 35, 7, 17)
    $g.FillRectangle($outline, $x + 17, $y + 35, 7, 17)
    $outline.Dispose()
    $body.Dispose()
}

function Draw-Spark($g, $x, $y, $color) {
    $p = Pen $color 2
    $g.DrawLine($p, $x - 4, $y, $x + 4, $y)
    $g.DrawLine($p, $x, $y - 4, $x, $y + 4)
    $p.Dispose()
}

function Draw-BasicNenIcon($name) {
    $file = Join-Path $OutDir "$name.png"
    $bmp = [System.Drawing.Bitmap]::new(64, 64, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.Clear([System.Drawing.Color]::Transparent)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::None
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
    $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half

    switch ($name) {
        "ten" {
            Draw-Person $g 16 8 "#8f958e"
            Draw-Arc $g ([System.Drawing.Rectangle]::new(8, 8, 48, 48)) 0 360 "#69d7ff" 4
            Draw-Arc $g ([System.Drawing.Rectangle]::new(13, 13, 38, 38)) 0 360 "#b8ecff" 2
        }
        "zetsu" {
            Draw-Person $g 16 8 "#777c77"
            $g.DrawLine((Pen "#151515" 8), 12, 12, 52, 52)
            $g.DrawLine((Pen "#d83b3b" 5), 12, 12, 52, 52)
            Draw-Spark $g 48 16 "#d83b3b"
        }
        "ren" {
            Draw-Person $g 16 8 "#8f958e"
            Draw-Arc $g ([System.Drawing.Rectangle]::new(6, 4, 52, 56)) 210 300 "#8f5bff" 5
            Draw-Arc $g ([System.Drawing.Rectangle]::new(12, 10, 40, 44)) 210 300 "#c190ff" 3
            Draw-Spark $g 51 17 "#d7b4ff"
            Draw-Spark $g 13 48 "#d7b4ff"
        }
        "en" {
            Draw-Person $g 16 8 "#8f958e"
            Draw-Arc $g ([System.Drawing.Rectangle]::new(3, 3, 58, 58)) 0 360 "#69d7ff" 3
            Draw-Arc $g ([System.Drawing.Rectangle]::new(10, 10, 44, 44)) 0 360 "#9eeaff" 2
            Draw-Arc $g ([System.Drawing.Rectangle]::new(17, 17, 30, 30)) 0 360 "#d5f7ff" 1
        }
        "ko" {
            Draw-Person $g 8 8 "#8f958e"
            $g.FillRectangle((Brush "#151515"), 38, 29, 18, 13)
            $g.FillRectangle((Brush "#f1d450"), 40, 31, 14, 9)
            Draw-Spark $g 55 31 "#fff4a3"
            Draw-Spark $g 47 24 "#f1d450"
        }
        "ken" {
            Draw-Person $g 16 8 "#8f958e"
            Draw-Arc $g ([System.Drawing.Rectangle]::new(7, 5, 50, 54)) 0 360 "#4fd9a9" 5
            Draw-Arc $g ([System.Drawing.Rectangle]::new(13, 11, 38, 42)) 0 360 "#b6ffe7" 2
            $g.DrawRectangle((Pen "#4fd9a9" 2), 20, 20, 24, 30)
        }
        "ryu" {
            Draw-Person $g 10 8 "#8f958e"
            $g.FillRectangle((Brush "#151515"), 39, 28, 16, 13)
            $g.FillRectangle((Brush "#69d7ff"), 41, 30, 12, 9)
            Draw-Arc $g ([System.Drawing.Rectangle]::new(10, 8, 44, 48)) 230 160 "#69d7ff" 4
            Draw-Spark $g 51 25 "#b8ecff"
        }
        "emperor_time" {
            Draw-Person $g 16 8 "#8f958e"
            Draw-Arc $g ([System.Drawing.Rectangle]::new(9, 7, 46, 50)) 0 360 "#d83b3b" 4
            Draw-Arc $g ([System.Drawing.Rectangle]::new(15, 13, 34, 38)) 0 360 "#f3d36e" 2
            Draw-Spark $g 16 17 "#d83b3b"
            Draw-Spark $g 48 17 "#d83b3b"
        }
    }

    $bmp.Save($file, [System.Drawing.Imaging.ImageFormat]::Png)
    $g.Dispose()
    $bmp.Dispose()
    Write-Host "Restored $name.png"
}

@("ten", "zetsu", "ren", "en", "ko", "ken", "ryu", "emperor_time") | ForEach-Object {
    Draw-BasicNenIcon $_
}
