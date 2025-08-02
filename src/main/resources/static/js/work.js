let selectedVariety = null;
let selectedPrice = null;

// 自动轮播相关变量
let bannerContainer = document.getElementById('bannerContainer');
let bannerItems = document.querySelectorAll('.banner-item');
let currentIndex = 0;
const intervalTime = 5000; // 5秒切换一次
let autoScrollInterval;

// 页面加载完成后启动自动轮播
window.onload = function () {
    console.log(bannerContainer, bannerItems); // 检查是否拿到 DOM
    startAutoScroll();
};

// 开始自动轮播
function startAutoScroll() {
    // console.log('startAutoScroll 执行'); // 加这行
    autoScrollInterval = setInterval(() => {
        // console.log('定时器触发'); // 加这行
        currentIndex = (currentIndex + 1) % bannerItems.length;
        scrollToCurrentIndex();
    }, intervalTime);

    // 鼠标悬停时停止自动轮播
    const bannerScroll = document.getElementById('bannerScroll');
    bannerScroll.addEventListener('mouseenter', () => {
        clearInterval(autoScrollInterval);
    });

    // 鼠标离开时重新启动自动轮播
    bannerScroll.addEventListener('mouseleave', () => {
        startAutoScroll();
    });
}

// 滚动到当前索引的图片
function scrollToCurrentIndex() {
    let scrollWidth = bannerItems[currentIndex].offsetLeft;
    // console.log('滚动距离:', scrollWidth); // 加这行
    bannerContainer.scrollTo({
        left: scrollWidth,
        behavior: 'smooth' // 平滑滚动效果
    });
}

// 原有选择商品的函数
// function selectVariety(card, variety, price) {
//     // 重置之前选中的卡片
//     document.querySelectorAll('.variety-card').forEach(item => {
//         item.classList.remove('selected');
//     });
//     // 标记当前选中
//     card.classList.add('selected');
//     // 赋值到表单
//     selectedVariety = variety;
//     selectedPrice = price;
//     document.getElementById('selectedVariety').value = variety;
//     document.getElementById('selectedPrice').value = price;
// }
window.scrollToCurrentIndex = scrollToCurrentIndex;
window.startAutoScroll = startAutoScroll;