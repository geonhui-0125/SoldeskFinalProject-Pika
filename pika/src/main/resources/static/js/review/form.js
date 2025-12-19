document.addEventListener('DOMContentLoaded', function() {
    // Character counting for textarea
    const textarea = document.getElementById('content');
    if (textarea) {
        const maxLength = textarea.getAttribute('maxlength');
        const charCountDisplay = document.createElement('p');
        charCountDisplay.style.fontSize = '0.8em';
        charCountDisplay.style.color = '#666';
        charCountDisplay.style.marginTop = '5px';
        textarea.closest('div').appendChild(charCountDisplay);

        function updateCharCount() {
            const currentLength = textarea.value.length;
            charCountDisplay.textContent = `글자 수: ${currentLength} / ${maxLength}`;
            if (currentLength > maxLength) {
                charCountDisplay.style.color = 'red';
            } else {
                charCountDisplay.style.color = '#666';
            }
        }
        textarea.addEventListener('input', updateCharCount);
        updateCharCount(); // Initial count
    }

    // Star rating functionality
    const starRatingContainer = document.querySelector('.star-rating');
    if (starRatingContainer) {
        const scoreInput = document.getElementById('scoreValue'); // Hidden input for score submission
        const radioButtons = starRatingContainer.querySelectorAll('input[type="radio"]');
        const labels = starRatingContainer.querySelectorAll('label');

        // Function to apply colors based on a score for LTR
        function applyStarColors(score) {
            labels.forEach(label => {
                const starValue = parseInt(label.htmlFor.replace('star', ''));
                const icon = label.querySelector('i'); // Get the Font Awesome icon
                if (icon) {
                    if (starValue <= score) { // Fill from left to the selected star
                        icon.style.color = '#ffcc00'; // Filled color
                    } else {
                        icon.style.color = '#ccc'; // Empty color
                    }
                }
            });
        }

        // Set initial score if pre-filled (e.g., for edit mode)
        let initialScore = scoreInput.value ? parseInt(scoreInput.value) : 0;
        if (initialScore > 0) {
            starRatingContainer.querySelector(`#star${initialScore}`).checked = true;
            applyStarColors(initialScore);
        }

        starRatingContainer.addEventListener('click', function(event) {
            if (event.target.tagName === 'LABEL' || event.target.tagName === 'I') { // Handle click on label or icon
                const targetLabel = (event.target.tagName === 'I') ? event.target.closest('label') : event.target;
                if (!targetLabel) return;

                const clickedStar = parseInt(targetLabel.htmlFor.replace('star', ''));
                scoreInput.value = clickedStar; // Update hidden input
                radioButtons.forEach(radio => {
                    if (parseInt(radio.value) === clickedStar) {
                        radio.checked = true;
                    } else {
                        radio.checked = false;
                    }
                });
                applyStarColors(clickedStar); // Apply colors immediately on click
            }
        });

        starRatingContainer.addEventListener('mouseover', function(event) {
            if (event.target.tagName === 'LABEL' || event.target.tagName === 'I') { // Handle hover on label or icon
                const targetLabel = (event.target.tagName === 'I') ? event.target.closest('label') : event.target;
                if (!targetLabel) return;

                const hoveredStar = parseInt(targetLabel.htmlFor.replace('star', ''));
                applyStarColors(hoveredStar); // Apply colors based on hover
            }
        });

        starRatingContainer.addEventListener('mouseout', function() {
            // Reapply colors based on currently selected score, or default if none
            const currentSelectedScore = scoreInput.value ? parseInt(scoreInput.value) : 0;
            applyStarColors(currentSelectedScore);
        });

        // Ensure form submission uses the scoreInput value (redundant with radio, but safer)
        const form = scoreInput.closest('form');
        if (form) {
             form.addEventListener('submit', function() {
                const selectedRadioButton = starRatingContainer.querySelector('input[type="radio"]:checked');
                if (selectedRadioButton) {
                    scoreInput.value = selectedRadioButton.value;
                }
            });
        }
    }
});