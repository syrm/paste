$('.dropdown-toggle').dropdown()

$('input[type=file]').change(function() {
    var file = $(this).val().split('\\').pop()
    $('#file').text(file)
})

$('li').click(function(elt) {
    $('#button-dd').html($(elt.target).text() + ' <span class="caret"></span>')
    $('#lexer').val($(elt.target).attr('data-id'))
})