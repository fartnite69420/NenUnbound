Add-Type -AssemblyName System.Drawing

$OutDir = Join-Path $PSScriptRoot "..\src\main\resources\assets\huntercraft\textures\gui\abilities"
$Skip = @(
    "flash_cleave.png", "ghost_step.png", "lion_fang_draw.png", "phantom_ring.png", "void_rend.png",
    "ten.png", "zetsu.png", "ren.png", "en.png", "ko.png", "ken.png", "ryu.png", "emperor_time.png",
    "double_jump.png", "dash.png", "guard.png"
)

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

function Draw-Person($g, $x, $y, $scale, $fill = "#8d918a", $outline = "#151515") {
    $o = Brush $outline; $f = Brush $fill; $s = $scale
    $g.FillEllipse($o, $x + 7*$s, $y, 10*$s, 10*$s)
    $g.FillEllipse($f, $x + 9*$s, $y + 2*$s, 6*$s, 6*$s)
    $g.FillRectangle($o, $x + 8*$s, $y + 10*$s, 8*$s, 16*$s)
    $g.FillRectangle($f, $x + 10*$s, $y + 12*$s, 4*$s, 12*$s)
    $g.FillRectangle($o, $x + 4*$s, $y + 13*$s, 4*$s, 13*$s)
    $g.FillRectangle($o, $x + 16*$s, $y + 13*$s, 4*$s, 13*$s)
    $g.FillRectangle($o, $x + 7*$s, $y + 25*$s, 5*$s, 13*$s)
    $g.FillRectangle($o, $x + 13*$s, $y + 25*$s, 5*$s, 13*$s)
    $g.FillRectangle($f, $x + 9*$s, $y + 25*$s, 2*$s, 10*$s)
    $g.FillRectangle($f, $x + 14*$s, $y + 25*$s, 2*$s, 10*$s)
    $o.Dispose(); $f.Dispose()
}

function Draw-Chain($g, $x1, $y1, $x2, $y2, $accent = "#b4b5aa") {
    $outline = Pen "#1b1b1b" 5
    $line = Pen $accent 3
    $g.DrawLine($outline, $x1, $y1, $x2, $y2)
    $g.DrawLine($line, $x1, $y1, $x2, $y2)
    $steps = 5
    for ($i = 0; $i -le $steps; $i++) {
        $t = $i / [double]$steps
        $x = [int]($x1 + ($x2 - $x1) * $t)
        $y = [int]($y1 + ($y2 - $y1) * $t)
        $g.DrawRectangle((Pen "#1b1b1b" 2), $x - 4, $y - 3, 8, 6)
        $g.DrawRectangle((Pen $accent 1), $x - 3, $y - 2, 6, 4)
    }
    $outline.Dispose(); $line.Dispose()
}

function Draw-Arc($g, $rect, $start, $sweep, $color, $width = 4) {
    $g.DrawArc((Pen "#151515" ($width + 2)), $rect, $start, $sweep)
    $g.DrawArc((Pen $color $width), $rect, $start, $sweep)
}

function Draw-Spark($g, $x, $y, $color) {
    $p = Pen $color 2
    $g.DrawLine($p, $x - 3, $y, $x + 3, $y)
    $g.DrawLine($p, $x, $y - 3, $x, $y + 3)
    $p.Dispose()
}

function Draw-Fist($g, $x, $y, $accent) {
    $o = Brush "#151515"; $a = Brush $accent; $d = Brush "#9b7b26"
    $g.FillRectangle($o, $x, $y + 7, 23, 16)
    $g.FillRectangle($a, $x + 2, $y + 9, 19, 12)
    for ($i = 0; $i -lt 4; $i++) {
        $g.FillRectangle($o, $x + 2 + $i*5, $y, 5, 11)
        $g.FillRectangle($d, $x + 3 + $i*5, $y + 2, 3, 7)
    }
    $g.FillRectangle($o, $x + 7, $y + 23, 10, 10)
    $g.FillRectangle($a, $x + 9, $y + 23, 6, 8)
    $o.Dispose(); $a.Dispose(); $d.Dispose()
}

function Draw-Icon($file) {
    $name = [System.IO.Path]::GetFileNameWithoutExtension($file)
    $bmp = [System.Drawing.Bitmap]::new(64, 64, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.Clear([System.Drawing.Color]::Transparent)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::None
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
    $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half

    $accent = "#67c7e8"
    if ($name -match "chain|judg|holy|steal|dowsing") { $accent = "#c7b35a" }
    elseif ($name -match "smoke|smokey") { $accent = "#a9b2b8" }
    elseif ($name -match "gum|elastic|texture|bungee") { $accent = "#4eb6e8" }
    elseif ($name -match "hammer|crossfire|liver|redirection|atlas|slip|breaker") { $accent = "#e0b72f" }
    elseif ($name -match "meteor|ankle|rising|whirlwind|tora|flow") { $accent = "#b14ce8" }
    elseif ($name -match "guard|aegis|heaven|mirror|skybreaker|steel|iron|perfect") { $accent = "#7db8e8" }
    elseif ($name -match "ten|ren|ko|ken|ryu|en|zetsu|emperor") { $accent = "#9b63e6" }

    if ($name -match "chain|steal|dowsing|judg|holy") {
        Draw-Person $g 6 15 1 "#8d918a"
        Draw-Chain $g 25 32 55 20 $accent
        if ($name -match "jail|holy") { Draw-Arc $g ([System.Drawing.Rectangle]::new(17, 8, 36, 42)) 25 310 $accent 4 }
        if ($name -match "steal") { Draw-Spark $g 54 18 "#ffffff"; Draw-Spark $g 48 27 $accent }
        if ($name -match "conjuration") { Draw-Arc $g ([System.Drawing.Rectangle]::new(11, 6, 42, 50)) 0 360 $accent 4 }
    } elseif ($name -match "smoke|smokey") {
        Draw-Person $g 8 17 1 "#8d918a"
        Draw-Arc $g ([System.Drawing.Rectangle]::new(22, 8, 34, 40)) 90 230 $accent 5
        Draw-Arc $g ([System.Drawing.Rectangle]::new(28, 17, 24, 27)) 120 250 "#d0d6da" 3
        Draw-Spark $g 50 15 "#d0d6da"
        if ($name -match "clone|soldier") { Draw-Person $g 34 17 1 "#b8bdb7" }
        if ($name -match "jail") { Draw-Arc $g ([System.Drawing.Rectangle]::new(10, 6, 44, 48)) 0 360 "#d0d6da" 3 }
    } elseif ($name -match "gum|elastic|texture|bungee") {
        Draw-Person $g 7 18 1 "#151515"
        if ($name -match "reflect") { Draw-Arc $g ([System.Drawing.Rectangle]::new(18, 8, 36, 42)) 0 360 $accent 5 }
        elseif ($name -match "trap") { Draw-Arc $g ([System.Drawing.Rectangle]::new(10, 36, 45, 16)) 0 360 $accent 4 }
        elseif ($name -match "texture") { $g.FillRectangle((Brush "#342047"), 30, 12, 24, 34); Draw-Spark $g 54 15 $accent }
        else { Draw-Chain $g 24 33 55 24 $accent }
    } elseif ($name -match "hammer|crossfire|liver|redirection|atlas|aegis") {
        Draw-Person $g 6 18 1 "#8d918a"
        Draw-Fist $g 32 18 $accent
        Draw-Spark $g 53 22 "#fff6a0"
        if ($name -match "crossfire|atlas|aegis") { Draw-Arc $g ([System.Drawing.Rectangle]::new(8, 35, 45, 16)) 0 360 $accent 4 }
    } elseif ($name -match "meteor|ankle|rising|whirlwind|tora") {
        Draw-Person $g 20 8 1 "#8d918a"
        Draw-Arc $g ([System.Drawing.Rectangle]::new(7, 31, 50, 20)) 0 360 $accent 4
        if ($name -match "rising") { $g.DrawLine((Pen $accent 5), 34, 48, 34, 12) }
        elseif ($name -match "meteor") { $g.DrawLine((Pen $accent 6), 33, 11, 21, 50) }
        elseif ($name -match "whirlwind") { Draw-Arc $g ([System.Drawing.Rectangle]::new(14, 8, 40, 46)) 285 260 $accent 5 }
        else { $g.DrawLine((Pen $accent 5), 22, 34, 48, 22) }
    } elseif ($name -match "guard|heaven|mirror|skybreaker|steel") {
        Draw-Person $g 8 18 1 "#8d918a"
        Draw-Arc $g ([System.Drawing.Rectangle]::new(25, 10, 25, 42)) 270 180 $accent 5
        if ($name -match "heaven|skybreaker") { $g.DrawLine((Pen $accent 6), 42, 8, 22, 54); Draw-Spark $g 23 53 "#ffffff" }
        if ($name -match "mirror") { $g.FillRectangle((Brush "#b8d4e7"), 35, 18, 15, 28) }
        if ($name -match "steel") { Draw-Arc $g ([System.Drawing.Rectangle]::new(9, 35, 45, 16)) 0 360 "#c9d1d7" 4 }
    } elseif ($name -match "ten|ren|ko|ken|ryu|en|zetsu|emperor") {
        Draw-Person $g 20 18 1 "#8d918a"
        if ($name -match "en|ken|ren|ten") { Draw-Arc $g ([System.Drawing.Rectangle]::new(8, 7, 48, 50)) 0 360 $accent 4 }
        if ($name -match "ko|ryu") { Draw-Fist $g 36 26 $accent }
        if ($name -match "zetsu") { $g.DrawLine((Pen "#151515" 7), 14, 15, 50, 49); $g.DrawLine((Pen "#d94a4a" 4), 14, 15, 50, 49) }
        if ($name -match "emperor") { Draw-Spark $g 16 16 "#e03a3a"; Draw-Spark $g 48 16 "#e03a3a"; Draw-Arc $g ([System.Drawing.Rectangle]::new(11, 10, 42, 44)) 0 360 "#e03a3a" 3 }
    } elseif ($name -match "dash|jump|guard") {
        Draw-Person $g 18 17 1 "#8d918a"
        if ($name -match "dash") { $g.DrawLine((Pen $accent 4), 8, 24, 29, 24); $g.DrawLine((Pen $accent 3), 6, 34, 25, 34) }
        elseif ($name -match "jump") { $g.DrawLine((Pen $accent 4), 31, 50, 31, 16); Draw-Spark $g 31 15 "#ffffff" }
        else { Draw-Arc $g ([System.Drawing.Rectangle]::new(31, 12, 20, 40)) 270 180 $accent 5 }
    } else {
        Draw-Person $g 20 18 1 "#8d918a"
        Draw-Spark $g 44 22 $accent
        Draw-Arc $g ([System.Drawing.Rectangle]::new(10, 32, 44, 16)) 0 360 $accent 3
    }

    $bmp.Save($file, [System.Drawing.Imaging.ImageFormat]::Png)
    $g.Dispose()
    $bmp.Dispose()
}

Get-ChildItem -LiteralPath $OutDir -Filter "*.png" | Where-Object { $Skip -notcontains $_.Name } | ForEach-Object {
    Draw-Icon $_.FullName
    Write-Host "Generated $($_.Name)"
}
