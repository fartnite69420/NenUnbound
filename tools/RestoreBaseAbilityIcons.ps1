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

function Draw-Spark($g, $x, $y, $color) {
    $p = Pen $color 2
    $g.DrawLine($p, $x - 4, $y, $x + 4, $y)
    $g.DrawLine($p, $x, $y - 4, $x, $y + 4)
    $p.Dispose()
}

function Draw-BaseIcon($name) {
    $file = Join-Path $OutDir "$name.png"
    $bmp = [System.Drawing.Bitmap]::new(64, 64, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.Clear([System.Drawing.Color]::Transparent)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::None
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
    $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half

    switch ($name) {
        "double_jump" {
            $g.DrawLine((Pen "#101010" 7), 32, 52, 32, 15)
            $g.DrawLine((Pen "#77d7ff" 4), 32, 52, 32, 15)
            $g.FillPolygon((Brush "#101010"), @(
                [System.Drawing.Point]::new(32, 6),
                [System.Drawing.Point]::new(20, 20),
                [System.Drawing.Point]::new(28, 20),
                [System.Drawing.Point]::new(28, 32),
                [System.Drawing.Point]::new(36, 32),
                [System.Drawing.Point]::new(36, 20),
                [System.Drawing.Point]::new(44, 20)
            ))
            $g.FillPolygon((Brush "#d8f4ff"), @(
                [System.Drawing.Point]::new(32, 10),
                [System.Drawing.Point]::new(25, 18),
                [System.Drawing.Point]::new(30, 18),
                [System.Drawing.Point]::new(30, 29),
                [System.Drawing.Point]::new(34, 29),
                [System.Drawing.Point]::new(34, 18),
                [System.Drawing.Point]::new(39, 18)
            ))
            Draw-Spark $g 18 45 "#77d7ff"
            Draw-Spark $g 47 45 "#77d7ff"
        }
        "dash" {
            $g.DrawLine((Pen "#101010" 8), 10, 24, 42, 24)
            $g.DrawLine((Pen "#77d7ff" 5), 10, 24, 42, 24)
            $g.DrawLine((Pen "#101010" 7), 6, 36, 34, 36)
            $g.DrawLine((Pen "#9be8ff" 4), 6, 36, 34, 36)
            $g.FillPolygon((Brush "#101010"), @(
                [System.Drawing.Point]::new(42, 15),
                [System.Drawing.Point]::new(58, 30),
                [System.Drawing.Point]::new(42, 45)
            ))
            $g.FillPolygon((Brush "#d8f4ff"), @(
                [System.Drawing.Point]::new(44, 21),
                [System.Drawing.Point]::new(53, 30),
                [System.Drawing.Point]::new(44, 39)
            ))
        }
        "guard" {
            $g.FillPolygon((Brush "#101010"), @(
                [System.Drawing.Point]::new(32, 7),
                [System.Drawing.Point]::new(51, 15),
                [System.Drawing.Point]::new(48, 39),
                [System.Drawing.Point]::new(32, 56),
                [System.Drawing.Point]::new(16, 39),
                [System.Drawing.Point]::new(13, 15)
            ))
            $g.FillPolygon((Brush "#8fd7ff"), @(
                [System.Drawing.Point]::new(32, 12),
                [System.Drawing.Point]::new(46, 18),
                [System.Drawing.Point]::new(43, 37),
                [System.Drawing.Point]::new(32, 49),
                [System.Drawing.Point]::new(21, 37),
                [System.Drawing.Point]::new(18, 18)
            ))
            $g.FillPolygon((Brush "#d8f4ff"), @(
                [System.Drawing.Point]::new(32, 17),
                [System.Drawing.Point]::new(40, 21),
                [System.Drawing.Point]::new(38, 35),
                [System.Drawing.Point]::new(32, 42),
                [System.Drawing.Point]::new(26, 35),
                [System.Drawing.Point]::new(24, 21)
            ))
            $g.DrawLine((Pen "#101010" 3), 32, 13, 32, 48)
        }
    }

    $bmp.Save($file, [System.Drawing.Imaging.ImageFormat]::Png)
    $g.Dispose()
    $bmp.Dispose()
    Write-Host "Restored $name.png"
}

@("double_jump", "dash", "guard") | ForEach-Object {
    Draw-BaseIcon $_
}
