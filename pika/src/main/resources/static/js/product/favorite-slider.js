document.addEventListener('DOMContentLoaded', function () {
    const sliderInner = document.querySelector('.slider_fp_inner');
    const prevBtn = document.querySelector('.prev_fp');
    const nextBtn = document.querySelector('.next_fp');
    const sliderWrap = document.querySelector('.slider_fp_wrap');

    let pages = [];          // 페이지 단위
    let currentIndex = 0;    // 현재 페이지 index
    const itemsPerPage = 2;  // 세로로 2개
    const pageWidth = 80;    // 한 페이지 너비 (CSS와 반드시 동일)

    // 찜한 상품 목록 가져오기
    async function fetchFavoriteProducts() {
        try {
            const response = await fetch('/products/api/favorites');
            if (!response.ok) {
                throw new Error('찜한 상품을 불러오는데 실패했습니다.');
            }

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

            // 마지막 페이지가 1개일 경우
            if (pageItems.length === 1) {
                page.classList.add('single');
            }

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
    }

    // 찜한 상품 없을 때
    function showEmptyMessage(message = '찜한 상품이 없습니다.') {
        sliderWrap.innerHTML =
            `<p style="text-align:center; padding:20px; font-size:12px;">${message}</p>`;
        sliderWrap.style.height = '150px';
        sliderWrap.style.justifyContent = 'center';
    }

    // ⭐ X축 슬라이드
    function updateSlide() {
        sliderInner.style.transform = `translateX(-${currentIndex * pageWidth}px)`;
    }

    // 다음 버튼
    nextBtn.addEventListener('click', () => {
        if (pages.length === 0) return;

        currentIndex = (currentIndex + 1) % pages.length;
        updateSlide();
    });

    // 이전 버튼
    prevBtn.addEventListener('click', () => {
        if (pages.length === 0) return;

        currentIndex =
            (currentIndex - 1 + pages.length) % pages.length;
        updateSlide();
    });

    // 초기 로드
    fetchFavoriteProducts();

    window.addFavoriteItem = function (item) {
        if (!sliderInner || !Array.isArray(pages)) return;

        const a = document.createElement('a');
        a.href = `/products/info/${item.productId}`;

        const img = document.createElement('img');
        img.src = item.productImage;
        img.alt = item.title || '';

        a.appendChild(img);

        let lastPage = pages[pages.length - 1];

        // 마지막 페이지가 있고, 1개만 차있을 때
        if (lastPage && lastPage.querySelectorAll('a').length === 1) {
            lastPage.appendChild(a);
            lastPage.classList.remove('single');
        }
        // 그 외 → 새 페이지 생성
        else {
            lastPage = document.createElement('div');
            lastPage.className = 'slider_fp_page single';

            lastPage.appendChild(a);
            sliderInner.appendChild(lastPage);
            pages.push(lastPage);
        }

        // ⭐ 핵심: 마지막 페이지로 이동
        currentIndex = pages.length - 1;
        updateSlide();
    };


    window.removeFavoriteItem = function (productId) {
        if (!sliderInner || !Array.isArray(pages)) return;
        
        console.log('상품 아이디 확인' + productId)

        const targetHref = `/products/info/${productId}`;
        const targetLink = sliderInner.querySelector(`a[href="${targetHref}"]`);

        if (!targetLink) return; // 이미 제거됨

        const page = targetLink.closest('.slider_fp_page');

        targetLink.remove();

        // page가 없으면 그냥 슬라이드 갱신만
        if (!page) {
            updateSlide();
            return;
        }

        const remainCount = page.querySelectorAll('a').length;

        // 페이지 비었으면 제거
        if (remainCount === 0) {
            page.remove();
            pages = pages.filter(p => p !== page);

            if (currentIndex >= pages.length) {
                currentIndex = Math.max(0, pages.length - 1);
            }
        }
        // 1개만 남았으면 single
        else if (remainCount === 1) {
            page.classList.add('single');
        }

        // 페이지 하나도 없으면
        if (pages.length === 0) {
            showEmptyMessage();
            return;
        }

        updateSlide();
    };

});


