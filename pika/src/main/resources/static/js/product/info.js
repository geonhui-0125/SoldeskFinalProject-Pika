// product-detail.js
document.querySelectorAll('.thumbnail').forEach(img => {
    img.addEventListener('click', () => {
        document.querySelector('.main-image img').src = img.src;

        document.querySelectorAll('.thumbnail')
            .forEach(t => t.classList.remove('active'));

        img.classList.add('active');
    });
});

