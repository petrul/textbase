#! /bin/bash

THUMB200="thumbs/200px"
mkdir -p $THUMB200
for F in *.jpg; do     
    BASENAME=`basename $F .jpg`
    OUTFILE="$THUMB200/$BASENAME.webp"
    if test ! -f "$OUTFILE" ; then
        echo convert -thumbnail 200x200^ "$F" "$OUTFILE"
	convert -thumbnail 200x200^ "$F" "$OUTFILE"
	git add "$F"
	git add "$OUTFILE"
    fi


done
#convert -thumbnail 200x200^ about.jpg a.webp
