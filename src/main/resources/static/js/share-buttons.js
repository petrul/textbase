// code for showing social media buttons on selection





// $(function(){

//     function insertButton() {

//         var selectionButton = new Element(
//                 'span', {
//                   'className':'nytd_selection_button',
//                   'id':'nytd_selection_button',
//                   'title':'Lookup Word',
//                   'style': 'margin:-20px 0 0 -20px; position:absolute; background:url(http://graphics8.nytimes.com/images/global/word_reference/ref_bubble.png);width:25px;height:29px;cursor:pointer;_background-image: none;filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src="http://graphics8.nytimes.com/images/global/word_reference/ref_bubble.png", sizingMethod="image");'
//                 }
//             )
        
//         if (Prototype.Browser.IE) {
//           var tmp = new Element('div');
//           tmp.appendChild(selectionButton);
//           newRange = selection.duplicate();
//           newRange.setEndPoint( "StartToEnd", selection);
//           newRange.pasteHTML(tmp.innerHTML);
//           selectionButton = $('nytd_selection_button');
//         }
//         else {
//           var range = selection.getRangeAt(0);
//           newRange = document.createRange();
//           newRange.setStart(selection.focusNode, range.endOffset);
//           newRange.insertNode(selectionButton);
//         }
//     }


//     // $(document.body).bind('mousedown', function(e){
//     //     console.log('mousedown');
//     //     var selection;
        
//     //     if (window.getSelection) {
//     //       selection = window.getSelection();
//     //     } else if (document.selection) {
//     //       selection = document.selection.createRange();
//     //     }

//     //     if (selection.toString != '') {
//     //         but.show();
//     //     } else {
//     //         but.hide();
//     //     }
//     // });

//     $(document.body).bind('mouseup', function(e){
//         var selection;
        
//         if (window.getSelection) {
//           selection = window.getSelection();
//         } else if (document.selection) {
//           selection = document.selection.createRange();
//         }

//         // var but = $('#share-button1');
//         if (selection.toString() !== '') {
//             // selected something
//             // but.show();

//             // but.css('left',  e.target.getBoundingClientRect().left);
//             // but.css('top',  e.target.getBoundingClientRect().top);

//             // e.target.style['background-color'] = 'red';

//             // console.log(
//             //     'mouseup : e.pageX=' + e.pageX
//             //     + ', e.pageY=' + e.pageY
//             //     + ', e.target.getBoundingClientRect().left=' + e.target.getBoundingClientRect().left
//             //     + ', e.target.getBoundingClientRect().top=' + e.target.getBoundingClientRect().top
//             //     + ', e.target.getBoundingClientRect().right=' + e.target.getBoundingClientRect().right
//             //     + ', e.target.getBoundingClientRect().bottom=' + e.target.getBoundingClientRect().bottom
//             //     + ', e.target=' + e.target
//             // );

//             // console.log(
//             //     'but.left=' + but.css('left')
//             //     +', but.top=' + but.css('top')
//             //     +', but.style.display=' + but.css('display')
//             //     +', but.style.position=' + but.css('position')
//             // );
//             // alert('"' + selection.toString() 
//             // + '" was selected at ' 
//             // + e.pageX + '/' 
//             // + e.pageY + " -- "
//             // + e.target.getBoundingClientRect()
//             // // + selection.anchorNode.wholeText
//             // );

//             insertButton() ;

//         } 
//         // else {
//         //     // no selection
//         //     but.hide();
//         // }
//     });
// });