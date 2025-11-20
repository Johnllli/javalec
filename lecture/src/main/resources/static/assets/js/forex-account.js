$(document).ready(function() {
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
                        contentHtml += `<li><strong>${key}:</strong> ${data[key]}</li>`;
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
});
