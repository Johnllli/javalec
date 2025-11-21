$(document).ready(function() {
    // Function to load content dynamically
    function loadContent(url, targetSectionId, contentDivId, navLinkId) {
        // Hide all other main content sections
        $('#main > section').hide();

        // Show the target section
        $(targetSectionId).show();

        // Update active navigation link
        $('#nav ul li a').removeClass('active');
        $(navLinkId).addClass('active');

        // Load content via AJAX
        $.ajax({
            url: url,
            method: 'GET',
            success: function(data) {
                $(contentDivId).html(data);
            },
            error: function(jqXHR, textStatus, errorThrown) {
                $(contentDivId).html('<p style="color: red;">Error loading content: ' + textStatus + '</p>');
                console.error("Error fetching content from " + url + ":", textStatus, errorThrown);
            }
        });
    }

    // Event listener for Forex Account nav link
    $('#forex-account-nav-link').on('click', function(e) {
        e.preventDefault(); // Prevent default link behavior

        // Hide all other main content sections
        $('#main > section').hide();

        // Show the Forex Account section
        $('#forex-account-section').show();

        // Update active navigation link
        $('#nav ul li a').removeClass('active');
        $(this).addClass('active');

        // Fetch data from the /forex-account endpoint
        $.ajax({
            url: '/forex-account',
            method: 'GET',
            dataType: 'json',
            success: function(data) {
                let contentHtml = '<h3>Account Details:</h3>';
                contentHtml += '<ul>';
                for (const key in data) {
                    if (data.hasOwnProperty(key)) {
                        // Basic formatting, you can enhance this
                        contentHtml += `<li><strong>${key}:</strong> ${key}: ${data[key]}</li>`;
                    }
                }
                contentHtml += '</ul>';
                $('#forex-account-content').html(contentHtml);
            },
            error: function(jqXHR, textStatus, errorThrown) {
                $('#forex-account-content').html('<p style="color: red;">Error loading Forex account data: ' + textStatus + '</p>');
                console.error("Error fetching Forex account data:", textStatus, errorThrown);
            }
        });
    });

    // Event listener for Forex ActPrice nav link
    $('#forex-actprice-nav-link').on('click', function(e) {
        e.preventDefault(); // Prevent default link behavior
        loadContent('/forex-actprice', '#forex-actprice-section', '#forex-actprice-content', '#forex-actprice-nav-link');
    });

    // Event listener for Forex HistPrice nav link
    $('#forex-histprice-nav-link').on('click', function(e) {
        e.preventDefault(); // Prevent default link behavior
        loadContent('/forex-histprice', '#forex-histprice-section', '#forex-histprice-content', '#forex-histprice-nav-link');
    });
});
