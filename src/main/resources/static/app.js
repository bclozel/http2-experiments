var talks = new Bloodhound({
    datumTokenizer: Bloodhound.tokenizers.obj.whitespace('title', 'authors'),
    queryTokenizer: Bloodhound.tokenizers.whitespace,
    identify: function(obj) { return obj.title; },
    prefetch: '/talks.json'
});

$('#prefetch .typeahead').typeahead(null, {
    name: 'talks',
    display: 'title',
    source: talks,
    templates: {
        suggestion: function(data) {
            return "<div><strong>"+ data.title + "</strong>" + " – " + data.authors.join(", ") +"</div>";
        }
    }
});