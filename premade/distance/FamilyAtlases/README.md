This folder is probably not useful in the general case. It has fonts made from 4 textures into a libGDX texture atlas,
and the library you use would need to be able to read libGDX's texture atlas format, use AngelCode bitmap fonts, and be
configured to render the correct style given some cue. If you use SquidLib, that library can use the following fonts in
the parent directory do a similar task but without some of the hassle of an atlas:

  - Iosevka-Family-distance.fnt
  - Iosevka-Slab-Family-distance.fnt
  - GoMono-Family-distance.fnt

Those fonts are set up to use one .fnt file to refer to four styles (regular, bold, italic, and bold italic). To
contrast, the files in this directory use four different .fnt files, and that can cause a headache.
