document.addEventListener('DOMContentLoaded', function () {
    const sliderInner = document.querySelector('.slider_fp_inner');
    const prevBtn = document.querySelector('.prev_fp');
    const nextBtn = document.querySelector('.next_fp');
    const sliderWrap = document.querySelector('.slider_fp_wrap');

    let pages = [];
    let currentIndex = 0;
    const itemsPerPage = 2;
    const pageWidth = 80;

    async function fetchFavoriteProducts() {
        try {
            const response = await fetch('/products/api/favorites');
            if (!response.ok) throw new Error('찜한 상품을 불러오는데 실패했습니다.');

            const favoriteList = await response.json();
            if (favoriteList.length > 0) {
                createList(favoriteList);
            } else {
                showEmptyMessage();
            }
        } catch (error) {
            console.error(error);
            showEmptyMessage('오류가 발생했습니다.');
        }
    }

    function createList(items) {
        sliderInner.innerHTML = '';
        pages = [];
        currentIndex = 0;

        for (let i = 0; i < items.length; i += itemsPerPage) {
            const pageItems = items.slice(i, i + itemsPerPage);
            const page = document.createElement('div');
            page.className = 'slider_fp_page';

            if (pageItems.length === 1) page.classList.add('single');

            pageItems.forEach(item => {
                const a = document.createElement('a');
                a.href = `/products/info/${item.productId}`;
                const img = document.createElement('img');
                img.src = item.productImage || '/profile/default-profile.jpg';
                img.alt = item.title || '';
                a.appendChild(img);
                page.appendChild(a);
            });

            sliderInner.appendChild(page);
            pages.push(page);
        }

        updateSlide();
        updateNavButtons(); // 리스트 생성 후 버튼 상태 업데이트
    }

    function showEmptyMessage(message = '찜한 상품이 없습니다.') {
        sliderWrap.innerHTML = `
            <div style="display: flex; align-items: center; justify-content: center; width: 80px; margin: 0 auto;">
                <p style="text-align: center; font-size: 11px; color: #888; line-height: 1.4; word-break: keep-all;">
                    ${message}
                </p>
            </div>
        `;
        sliderWrap.classList.add("single");
        updateNavButtons(); // 빈 메시지 출력 시 버튼 숨김
    }

    function updateSlide() {
        if (!sliderInner) return;
        sliderInner.style.transform = `translateX(-${currentIndex * pageWidth}px)`;
    }

    function updateNavButtons() {
        // pages가 없거나 1개 이하면 버튼 숨김
        if (!pages || pages.length <= 1) {
            prevBtn.style.display = 'none';
            nextBtn.style.display = 'none';
        } else {
            prevBtn.style.display = 'block';
            nextBtn.style.display = 'block';
        }
    }

    nextBtn.addEventListener('click', () => {
        if (pages.length <= 1) return;
        currentIndex = (currentIndex + 1) % pages.length;
        updateSlide();
    });

    prevBtn.addEventListener('click', () => {
        if (pages.length <= 1) return;
        currentIndex = (currentIndex - 1 + pages.length) % pages.length;
        updateSlide();
    });

    fetchFavoriteProducts();

    window.addFavoriteItem = function (item) {
        const emptyMsg = sliderWrap.querySelector('p');
        if (emptyMsg) {
            location.reload();
            return;
        }

        if (!sliderInner) return;

        const a = document.createElement('a');
        a.href = `/products/info/${item.productId}`;
        const img = document.createElement('img');
        img.src = item.productImage || '/profile/default-profile.jpg';
        img.alt = item.title || '';
        a.appendChild(img);

        let lastPage = pages[pages.length - 1];

        if (!lastPage || lastPage.querySelectorAll('a').length >= itemsPerPage) {
            const newPage = document.createElement('div');
            newPage.className = 'slider_fp_page single';
            newPage.appendChild(a);
            sliderInner.appendChild(newPage);
            pages.push(newPage);
        } else {
            lastPage.appendChild(a);
            lastPage.classList.remove('single');
        }

        currentIndex = pages.length - 1;
        updateSlide();
        updateNavButtons(); // 아이템 추가 후 버튼 상태 업데이트
    };

    window.removeFavoriteItem = function (productId) {
        if (!sliderInner || !Array.isArray(pages)) return;

        const targetHref = `/products/info/${productId}`;
        const targetLink = sliderInner.querySelector(`a[href="${targetHref}"]`);
        if (!targetLink) return;

        targetLink.remove();

        const allRemainingItems = Array.from(sliderInner.querySelectorAll('a'));

        if (allRemainingItems.length === 0) {
            pages = [];
            showEmptyMessage();
            return;
        }

        sliderInner.innerHTML = '';
        pages = [];

        for (let i = 0; i < allRemainingItems.length; i += itemsPerPage) {
            const pageItems = allRemainingItems.slice(i, i + itemsPerPage);
            const page = document.createElement('div');
            page.className = 'slider_fp_page';
            if (pageItems.length === 1) page.classList.add('single');
            pageItems.forEach(item => page.appendChild(item));
            sliderInner.appendChild(page);
            pages.push(page);
        }

        if (currentIndex >= pages.length) {
            currentIndex = Math.max(0, pages.length - 1);
        }

        updateSlide();
        updateNavButtons(); // 아이템 삭제 후 버튼 상태 업데이트
    };
});