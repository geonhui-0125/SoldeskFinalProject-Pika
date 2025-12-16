// product-detail.js
document.querySelectorAll('.thumbnail').forEach(img => {
    img.addEventListener('click', () => {
        document.querySelector('.main-image img').src = img.src;

        document.querySelectorAll('.thumbnail')
            .forEach(t => t.classList.remove('active'));

        img.classList.add('active');
    });
});

document.querySelector(".like-btn").addEventListener('click', () => {

    const likeButton = document.querySelector(".like-btn");
    const productId = document.querySelector('.product-id').value;
    const wishedInput = document.querySelector('.wished'); // <input> 태그

    // 현재 찜 상태 확인 (boolean 값으로 변환)
    const isWished = wishedInput.value === 'true';

    // 찜 상태에 따라 사용할 HTTP 메소드 결정
    // isWished가 true (찜한 상태)이면 -> DELETE (취소)
    // isWished가 false (찜 안 한 상태)이면 -> POST (찜하기)
    const httpMethod = isWished ? "DELETE" : "POST";

    fetch(`/api/product/${productId}/wish`,
        { method: httpMethod })
        .then(resp => {
            if (resp.ok) {
                return resp.text();
            }
            // 서버에서 4xx/5xx 오류가 발생하면 이 Error를 throw 합니다.
            throw new Error(`요청 실패: ${resp.status}`);
        })
        .then(data => {
            const newWishCnt = parseInt(data);

            console.log('변화된 wishCnt : ' , newWishCnt);

            // 1. 찜 카운트 업데이트
            const wishCntElement = document.querySelector('.wish-cnt span');
            if (wishCntElement) {
                wishCntElement.textContent = newWishCnt;
            }

            // --- 2. 버튼 효과 및 wished 상태 업데이트 로직 ---

            // 상태 반전 (찜 취소했으면 false로, 찜 했으면 true로)
            const newIsWished = !isWished;

            // a. 버튼 클래스 토글 (버튼 효과 적용/제거)
            if (newIsWished) {
                likeButton.classList.add('wished-active'); // 찜하기 완료 -> 활성화 클래스 추가
            } else {
                likeButton.classList.remove('wished-active'); // 찜 취소 완료 -> 활성화 클래스 제거
            }

            // b. 숨겨진 wished 필드 값 업데이트 (다음 클릭에 대비하여 상태 저장)
            wishedInput.value = newIsWished.toString();

        })
        .catch(error => {
            console.error('찜 처리 중 오류 발생:', error);
            alert(`찜 처리 중 오류가 발생했습니다. (${error.message})`);
        });

});

