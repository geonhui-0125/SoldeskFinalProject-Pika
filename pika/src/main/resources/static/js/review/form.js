
    document.addEventListener('DOMContentLoaded', function() {
        const textarea = document.getElementById('content');
        if (textarea) { // Ensure textarea exists
            const maxLength = textarea.getAttribute('maxlength');
            const charCountDisplay = document.createElement('p');
            charCountDisplay.style.fontSize = '0.8em';
            charCountDisplay.style.color = '#666';
            charCountDisplay.style.marginTop = '5px'; // Add some margin

            // Insert the counter display right after the textarea's parent div
            textarea.closest('div').appendChild(charCountDisplay);


            function updateCharCount() {
                const currentLength = textarea.value.length;
                charCountDisplay.textContent = `글자 수: ${currentLength} / ${maxLength}`;
                if (currentLength > maxLength) {
                    charCountDisplay.style.color = 'red'; // Indicate overflow visually
                } else {
                    charCountDisplay.style.color = '#666';
                }
            }

            textarea.addEventListener('input', updateCharCount);
            updateCharCount(); // Initial count
        }
    });
