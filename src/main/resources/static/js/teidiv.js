$(function () {
  /* show footnotes */
  $('[data-toggle="tooltip"]').tooltip()
})

/* When the user scrolls down, hide the navbar. When the user scrolls up, show the navbar */
var prevScrollpos = window.pageYOffset;
window.onscroll = function() {
  const MARGE = 10
  var currentScrollPos = window.pageYOffset;

  if (currentScrollPos < MARGE) {
    // we're very close to top
    $('#navbar').fadeIn();
  }
  
  if (currentScrollPos > prevScrollpos + MARGE) {
    // we scrolled down significantly
    $('#navbar').fadeOut();
  }
  
  if (MARGE + currentScrollPos < prevScrollpos ) {
    // we scrolled up significantly
    $('#navbar').fadeIn();
  }
  prevScrollpos = currentScrollPos;
}

// function pageScroll() {
//   window.scrollBy(0,1);
//   scrolldelay = setTimeout(pageScroll,10);
// }

/**
 * a bit of code that detects 'poems' in the page. a poem is a group of lg's, 
 * a succession of stanzas. 
 * 
 * that is in order to calculate the widht of the poem, so all the stanzas can be aligned
 * left but the whole poem be centered.
 */
function recompute_stanzas_margins(){
  console.log('start recompute_stanzas_margins, innerwidth ' + window.innerWidth, 'outerwidth:' + window.outerWidth, window.innerWidth)

  var poems = []
  var crt_poem;
  var stanza2poem = new Map()

  // use width:max-content so that we can measure the actual width of the text
  $('div.lg').css('width', 'max-content')
  $('p.l').css('width', 'max-content')

  let stanzas = $('div.lg')

  stanzas.each ( function(idx, lg) {
    // a page may have several poems, for ex. inserted into prose. 
    // detect how many poems we have
    // console.log("each stanza", idx, lg, $(this), $(this).length, lg.nodeName, lg.className)
    var prev = lg.previousElementSibling;
    // console.log("prev", prev.nodeName, prev.className)
    if (prev) {
      var prevName = prev.nodeName
      var prevClass = prev.className
  
      if (prev && prevName != 'DIV' && prevClass != 'lg') {
        // this is the first stanza
        crt_poem = { 
          l_length_sum : 0, // total sum of l lines
          nr_l: 0,          // nr of lines (for average)
          get avg_l_length() { return this.l_length_sum / this.nr_l }, // average line length
          stanzas: [ lg ] 
        }
        poems.push(crt_poem)
      } 
  
      if (prev && prevName == 'DIV' && prevClass == 'lg') {
        // this is not the first stanza of a poem
        crt_poem.stanzas.push(lg)
      }

      stanza2poem.set(lg, crt_poem)
  
    }

    // console.log("lg", $(this), $(this).position().left,$(this).offset().left )
    let ls = $(this).find('.l')
    ls.each(function(idx, l) {
      // var w = $(this).width();
      var w = l.offsetWidth;
      console.log(`line:  ${l.innerHTML} : width = ${w}`)
      crt_poem.l_length_sum += w
      crt_poem.nr_l += 1

      // width:auto instead of max-content so that the long line 
      // does not go unbothered out of screen; lg stanza set a hard max limit
      $(this).css('width', 'auto')
    })
  });

  // $('p.l').each(function (idx, l) {
    
  // })

  for (p of poems) {
    console.log(`poem `, p)
  }

  var ww = window.innerWidth;
  var bodyw = $('.body-of-text').width()
  console.log('ww', ww, 'bodyw', bodyw)
  stanzas.each ( function(idx, lg) {
    
    var poem = stanza2poem.get(lg)
    var lg_width =  Math.round(poem.avg_l_length);
    console.log("setting margin of stanza of poem:", lg, poem, lg_width)
    
    if (lg_width < bodyw) {
      //var w = Math.min(lg_width, ww)

      var margin_left = Math.round((bodyw - lg_width) / 2);

      // TODO replace setting width with settting margin-left
      // $(this).css('width', "" + w + 'px')
      var existing_margin_left = parseInt($(this).css('margin-left'))
      console.log(`existing margin left ${$(this).css('margin-left')}`)
      // margin_left = existing_margin_left + margin_left
      $(this).css('margin-left', "" + margin_left + 'px')
      $(this).css('width', 'auto')
      // console.log(`setting ${lg.nodeName}  width: ${lg_width}px;`)  
      console.log(`setting margin left to ${margin_left} for stanza` ,  lg)  
    } else {
      console.log(`for this stanza window width is too small, will not modify width`, lg)
    }
  })
  // for(var lg in poezii) {
  //   console.log(lg)
  // }
  // $('body,html').animate({scrollTop: 156}, 800); 
}


$(document).ready(recompute_stanzas_margins);
window.addEventListener("resize", recompute_stanzas_margins);



function checkKey(e) {

  e = e || window.event;

  // if (e.keyCode == '38') {
  //     // up arrow
  // }
  // else if (e.keyCode == '40') {
  //     // down arrow
  // }
  if (e.keyCode == '37') {
     // left arrow
     prev = $("head link[rel='prev']")
     if (prev.length > 0) {
      prev_href = prev.attr('href')
       console.log(prev_href)
       window.location.href = prev_href;
     }
  }
  else if (e.keyCode == '39') {
    // right arrow

    next = $("head link[rel='next']")
    if (next.length > 0) {
     next_href=next.attr('href')
      console.log(next_href)
      window.location.href = next_href;
    }
  }

}
document.onkeydown = checkKey;