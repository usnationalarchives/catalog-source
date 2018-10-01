$(function(){
    $(window).bind("resize",function(){

        if($(this).width() <500){

            $('#ResultControls').removeClass('container-fluid').addClass('container')
        }
        else{
            $('#ResultControls').removeClass('container').addClass('container-fluid')

        }
    })
});

//Search a class on an element classes
function hasClass(element, cls) {
    return (' ' + element.className + ' ').indexOf(' ' + cls + ' ') > -1;
}

//This prevents prematurely closing of responsive menu when the touch event is over the menu
$(document).on('touchstart click', '#menuResponsive', function (event) {
    event.stopPropagation();
});

$(document).on('touchstart click', '#buttonMenuResponsive', function (event) {
  event.stopPropagation();
});

//This closes the responsive menu when is touch outside the menu
$(document).on('touchstart click', '#menuResponsiveParent', function () {
    if(hasClass(document.getElementById("menuResponsiveParent"),"in")){
        $('#menuResponsiveParent').collapse('hide');
    }
});
