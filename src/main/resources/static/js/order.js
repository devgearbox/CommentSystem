document.addEventListener('DOMContentLoaded', function () {
    // 核心静态元素（提前获取，避免重复查询）
    const orderTableBody = document.getElementById('order-table-body'); // 表格tbody（事件委托父容器）
    const selectAllOrder = document.getElementById('select-all-order'); // 全选框

    // ====================== 1. 搜索和查看全部订单 ======================
    const searchButton = document.getElementById('search-button');
    const searchAllButton = document.getElementById('search-all');
    const searchInput = document.getElementById('search-input');

    // 搜索订单
    searchButton.addEventListener('click', function () {
        const searchTerm = searchInput.value.trim();
        if (searchTerm) {
            fetch('/orders/search?supplierName=' + encodeURIComponent(searchTerm))
                .then(response => response.text())
                .then(html => {
                    orderTableBody.innerHTML = html; // 动态替换表格内容
                    resetBatchDeleteState(); // 搜索后重置批量删除状态
                    updateOrderChecks(); // 重新绑定复选框事件
                })
                .catch(error => {
                    console.error('搜索失败:', error);
                });
        }
    });

    // 查看全部订单
    searchAllButton.addEventListener('click', function () {
        window.location.href = '/orders';
    });


    // ====================== 2. 批量删除功能（修复动态勾选框） ======================
const batchDeleteBtn = document.getElementById('del-order');
if (batchDeleteBtn) { // 只有按钮存在时才执行后续绑定
    const confirmDeleteBtn = document.getElementById('confirm-delete-order');
    const cancelDeleteBtn = document.getElementById('cancel-delete-order');
    const selectAllOrder = document.getElementById('select-all-order');
    let isSelectVisible = false;

    // 点击"删除订单"显示勾选框
    batchDeleteBtn.addEventListener('click', () => {
        if (!isSelectVisible) {
            isSelectVisible = true;
            // 每次点击重新获取勾选框列（解决动态渲染后DOM失效）
            const selectColumns = document.querySelectorAll('th:nth-child(1), td:nth-child(1)');
            selectColumns.forEach(column => {
                column.style.display = 'table-cell';
            });
            if (confirmDeleteBtn) confirmDeleteBtn.style.display = 'inline-block';
            if (cancelDeleteBtn) cancelDeleteBtn.style.display = 'inline-block';
            updateOrderChecks(); // 重新绑定复选框事件
        }
    });

    // 取消批量删除（判断按钮存在）
    if (cancelDeleteBtn) {
        cancelDeleteBtn.addEventListener('click', resetBatchDeleteState);
    }

    // 全选/取消全选（判断全选框存在）
    if (selectAllOrder) {
        selectAllOrder.addEventListener('change', () => {
            const orderChecks = document.querySelectorAll('.order-check');
            orderChecks.forEach(check => check.checked = selectAllOrder.checked);
        });
    }

    // 更新订单复选框事件（动态渲染后重新绑定）
    function updateOrderChecks() {
        const orderChecks = document.querySelectorAll('.order-check');
        orderChecks.forEach(check => {
            check.removeEventListener('change', handleOrderCheckChange);
            check.addEventListener('change', handleOrderCheckChange);
        });
    }

    // 复选框变化：更新全选状态
    function handleOrderCheckChange() {
        const allChecks = document.querySelectorAll('.order-check');
        if (selectAllOrder) {
            selectAllOrder.checked = Array.from(allChecks).every(cb => cb.checked);
        }
    }

    // 重置批量删除状态（通用函数）
    function resetBatchDeleteState() {
        isSelectVisible = false;
        const selectColumns = document.querySelectorAll('th:nth-child(1), td:nth-child(1)');
        selectColumns.forEach(column => {
            column.style.display = 'none';
        });
        if (confirmDeleteBtn) confirmDeleteBtn.style.display = 'none';
        if (cancelDeleteBtn) cancelDeleteBtn.style.display = 'none';
        if (selectAllOrder) selectAllOrder.checked = false;
        // 重置所有复选框
        document.querySelectorAll('.order-check').forEach(check => check.checked = false);
    }

    // 确认批量删除（判断按钮存在）
    if (confirmDeleteBtn) {
        confirmDeleteBtn.addEventListener('click', async function () {
            const selectedIds = [];
            document.querySelectorAll('.order-check').forEach(check => {
                if (check.checked) selectedIds.push(parseInt(check.dataset.id));
            });

            if (selectedIds.length === 0) {
                alert('请选择要删除的订单');
                return;
            }

            try {
                const response = await fetch('/orders/delete/batch', {
                    method: 'DELETE',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ ids: selectedIds })
                });

                const result = await response.json();
                if (result.success) {
                    alert('批量删除订单成功');
                    window.location.reload();
                } else {
                    alert('删除失败: ' + (result.message || '未知错误'));
                }
            } catch (error) {
                console.error('删除请求失败:', error);
                alert('网络错误，删除失败');
            }
        });
    }
}

    // ====================== 3. 订单详情功能（事件委托强化） ======================
    const viewModal = document.getElementById('order-view-modal');
    const closeViewBtn = document.querySelector('#order-view-modal .close');
    const STATUS_MAP = {
        pending: "待审核",
        shipping: "待发货",
        shipped: "已发货",
        received: "已接收",
        rejected: "已拒收",
        cancelled: "已取消"
    };

    // 详情字段DOM映射
    const detailFields = {
        orderNo: document.getElementById('order-no'),
        orderRealName: document.getElementById('order-real-name'),
        orderPrice: document.getElementById('order-price'),
        orderQuantity: document.getElementById('order-quantity'),
        orderVariety: document.getElementById('order-variety'),
        orderTotalPrice: document.getElementById('order-total-price'),
        orderSupplierName: document.getElementById('order-supplier-name'),
        orderSupplierPhone: document.getElementById('order-supplier-phone'),
        orderSupplierAddress: document.getElementById('order-supplier-address'),
        orderStatus: document.getElementById('order-status'),
        orderDate: document.getElementById('order-date')
    };

    // 事件委托：支持按钮内部元素点击（如<i>标签）
    orderTableBody.addEventListener('click', async function (event) {
        // 找到最近的.view-btn（解决按钮内有图标时的点击穿透）
        const viewBtn = event.target.closest('.view-btn');
        if (viewBtn) {
            const orderId = viewBtn.getAttribute('data-id');
            if (!orderId) {
                alert('未获取到订单 ID');
                return;
            }

            try {
                const response = await fetch(`/orders/detail/${orderId}`);
                if (!response.ok) throw new Error('获取详情失败');
                const order = await response.json();

                // 填充弹窗数据
                detailFields.orderNo.textContent = order.orderNo || '无';
                detailFields.orderRealName.textContent = order.user?.real_name || '无';
                detailFields.orderPrice.textContent = order.litchiVariety?.price || '9.00';
                detailFields.orderQuantity.textContent = order.purchase_quantity || '0';
                detailFields.orderVariety.textContent = order.purchase_variety || '无';
                detailFields.orderTotalPrice.textContent = order.totalPrice || '0.00';
                detailFields.orderSupplierName.textContent = order.supplier?.supplier_name || '无';
                detailFields.orderSupplierPhone.textContent = order.supplier?.phone || '无';
                detailFields.orderSupplierAddress.textContent = order.supplier?.address || '无';
                detailFields.orderStatus.textContent = STATUS_MAP[order.orderStatus] || "未知状态";
                detailFields.orderDate.textContent = order.createTime
                    ? new Date(order.createTime).toLocaleString()
                    : '无';

                viewModal.style.display = 'flex';
            } catch (error) {
                console.error('查看订单详情失败：', error);
                alert('加载详情失败，请重试');
            }
        }
    });

    // 关闭详情弹窗
    closeViewBtn.addEventListener('click', () => {
        viewModal.style.display = 'none';
    });
    viewModal.addEventListener('click', (e) => {
        if (e.target === viewModal) viewModal.style.display = 'none';
    });


    // ====================== 4. 订单状态修改功能（事件委托强化） ======================
    const statusModal = document.getElementById('status-modal');
    const closeStatusBtn = document.querySelector('.status-close');
    const statusForm = document.getElementById('status-form');
    const statusOrderIdInput = document.getElementById('status-order-id');
    const currentStatusSpan = document.getElementById('current-status');
    const newStatusSelect = document.getElementById('new-status');
    const STATUS_FLOW = [
        { value: 'pending', label: '待审核' },
        { value: 'shipping', label: '待发货' },
        { value: 'shipped', label: '已发货' }
        // { value: 'received', label: '已接收' },
        // { value: 'cancelled', label: '已取消' }
    ];

    // 动态渲染状态下拉框
    function renderStatusOptions(currentStatus) {
        newStatusSelect.innerHTML = '';
        const currentIndex = STATUS_FLOW.findIndex(item => item.value === currentStatus);
        STATUS_FLOW.forEach((item, index) => {
            if (index >= currentIndex) {
                const option = document.createElement('option');
                option.value = item.value;
                option.textContent = item.label;
                if (item.value === currentStatus) option.selected = true;
                newStatusSelect.appendChild(option);
            }
        });
    }

    // 事件委托：支持按钮内部元素点击（如<i>标签）
    orderTableBody.addEventListener('click', function (event) {
        // 找到最近的.status-btn（解决按钮内有图标时的点击穿透）
        const statusBtn = event.target.closest('.status-btn');
        if (statusBtn) {
            const orderId = statusBtn.getAttribute('data-id');
            const currentStatus = statusBtn.getAttribute('data-status');

            // 已接收/已拒收状态不允许修改
            if (currentStatus === 'received' || currentStatus === 'rejected') {
                alert(`该订单状态为【${currentStatus === 'received' ? '已接收' : '已拒收'}】，请联系采购人协商`);
                return;
            }

            renderStatusOptions(currentStatus);
            statusOrderIdInput.value = orderId;
            currentStatusSpan.textContent = STATUS_MAP[currentStatus] || currentStatus;
            statusModal.style.display = 'block';
        }
    });

    // 提交状态修改表单
    statusForm.addEventListener('submit', function (e) {
        e.preventDefault();
        const orderId = statusOrderIdInput.value;
        const newStatus = newStatusSelect.value;

        fetch(`/api/orders/${orderId}/status?newStatus=${newStatus}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' }
        }).then(response => {
            if (response.ok) {
                location.reload();
            }
        }).catch(error => {
            console.error('修改状态失败:', error);
            alert('状态修改失败，请重试');
        });
    });

    // 关闭状态修改弹窗
    closeStatusBtn.addEventListener('click', () => {
        statusModal.style.display = 'none';
    });
    window.addEventListener('click', function (e) {
        if (e.target === statusModal) statusModal.style.display = 'none';
    });


    // ====================== 5. 其他弹窗关闭逻辑（统一处理） ======================
    // 新增订单弹窗关闭
    const addModal = document.getElementById('add-modal');
    const addModalClose = document.getElementById('add-modal-close');
    if (addModal && addModalClose) {
        addModalClose.addEventListener('click', () => {
            addModal.style.display = 'none';
        });
        window.addEventListener('click', (e) => {
            if (e.target === addModal) addModal.style.display = 'none';
        });
    }

    // 编辑订单弹窗关闭
    const editModal = document.getElementById('edit-modal');
    const editModalClose = document.querySelector('.edit-close');
    if (editModal && editModalClose) {
        editModalClose.addEventListener('click', () => {
            editModal.style.display = 'none';
        });
        window.addEventListener('click', (e) => {
            if (e.target === editModal) editModal.style.display = 'none';
        });
    }
});