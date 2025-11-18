document.addEventListener('DOMContentLoaded', function () {
    // 核心静态元素
    const supplierTableBody = document.getElementById('supplier-table-body');
    const selectAll = document.getElementById('select-all');
    const searchInput = document.getElementById('search-input');

    // ====================== 1. 搜索与查看全部 ======================
    document.getElementById('search-button').addEventListener('click', function() {
        const searchTerm = searchInput.value.trim();
        if (searchTerm) {
            // 获取当前分页参数
            const urlParams = new URLSearchParams(window.location.search);
            const page = urlParams.get('page') || 1;
            const size = urlParams.get('size') || 10;

            fetch(`/suppliers/search?name=${encodeURIComponent(searchTerm)}&page=${page}&size=${size}`)
                .then(response => response.text())
                .then(html => {
                    supplierTableBody.innerHTML = html;
                    resetBatchDeleteState();
                    updateSupplierChecks();
                })
                .catch(error => console.error('搜索失败:', error));
        }
    });

    // 查看全部也要支持分页
    document.getElementById('search-all').addEventListener('click', () => {
        const urlParams = new URLSearchParams(window.location.search);
        // 保留分页参数
        urlParams.set('page', '1');
        window.location.href = window.location.pathname + '?' + urlParams.toString();
    });


    // ====================== 2. 批量删除功能 ======================
    const batchDeleteBtn = document.getElementById('del-supplier');
    const confirmDeleteBtn = document.getElementById('confirm-delete');
    const cancelDeleteBtn = document.getElementById('cancel-delete');
    let isSelectVisible = false;

    // 显示批量删除选择框（关键修复：动态获取列）
    if (batchDeleteBtn) {
    batchDeleteBtn.addEventListener('click', () => {
        if (!isSelectVisible) {
            isSelectVisible = true;
            // 每次点击重新获取勾选框列（解决动态渲染后DOM失效）
            const selectColumns = document.querySelectorAll('th:nth-child(1), td:nth-child(1)');
            selectColumns.forEach(column => {
                column.style.display = 'table-cell';
            });
            confirmDeleteBtn.style.display = 'inline-block';
            cancelDeleteBtn.style.display = 'inline-block';
            updateSupplierChecks(); // 绑定新复选框事件
        }
    });
    }

    // 取消批量删除（关键修复：动态获取列）
    if (cancelDeleteBtn) {
    cancelDeleteBtn.addEventListener('click', resetBatchDeleteState);}

    // 全选/取消全选
    if (selectAll) {
    selectAll.addEventListener('change', () => {
        document.querySelectorAll('.supplier-check').forEach(cb => {
            cb.checked = selectAll.checked;
        });
    });
    }

    // 复选框事件处理函数
    function handleCheckChange() {
        const allChecks = document.querySelectorAll('.supplier-check');
        selectAll.checked = Array.from(allChecks).every(cb => cb.checked);
    }

    // 更新复选框事件（动态渲染后重新绑定）
    function updateSupplierChecks() {
        const supplierChecks = document.querySelectorAll('.supplier-check');
        supplierChecks.forEach(cb => {
            cb.removeEventListener('change', handleCheckChange);
            cb.addEventListener('change', handleCheckChange);
        });
    }

    // 重置批量删除状态（抽取为通用函数）
    function resetBatchDeleteState() {
        isSelectVisible = false;
        const selectColumns = document.querySelectorAll('th:nth-child(1), td:nth-child(1)');
        selectColumns.forEach(column => {
            column.style.display = 'none';
        });
        confirmDeleteBtn.style.display = 'none';
        cancelDeleteBtn.style.display = 'none';
        selectAll.checked = false;
        document.querySelectorAll('.supplier-check').forEach(cb => cb.checked = false);
    }

    // 确认批量删除
    confirmDeleteBtn.addEventListener('click', async () => {
        const selectedIds = [];
        document.querySelectorAll('.supplier-check').forEach(cb => {
            if (cb.checked) selectedIds.push(parseInt(cb.dataset.id));
        });

        if (selectedIds.length === 0) {
            alert('请选择要删除的供应商');
            return;
        }

        try {
            const res = await fetch('/delete/batch', {
                method: 'DELETE',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ ids: selectedIds })
            });
            const result = await res.json();

            if (result.success) {
                alert('批量删除成功');
                window.location.reload();
            } else {
                alert('删除失败: ' + (result.message || '未知错误'));
            }
        } catch (err) {
            console.error('删除请求失败:', err);
            alert('网络错误，删除失败');
        }
    });


    // ====================== 3. 详情查看功能 ======================
    const viewModal = document.getElementById('view-modal');
    const closeViewBtn = viewModal.querySelector('.close');
    const detailFields = {
        id: document.getElementById('detail-id'),
        status: document.getElementById('detail-status'),
        name: document.getElementById('detail-name'),
        contact: document.getElementById('detail-contact'),
        phone: document.getElementById('detail-phone'),
        address: document.getElementById('detail-address'),
        varieties: document.getElementById('detail-varieties'),
        cooperation: document.getElementById('detail-cooperation'),
        createTime: document.getElementById('detail-create-time'),
        updateTime: document.getElementById('detail-update-time'),
        orderCount: document.getElementById('detail-order-count')
    };

    // 事件委托：支持按钮内部元素点击
    supplierTableBody.addEventListener('click', async (e) => {
        const viewBtn = e.target.closest('.view-btn');
        if (viewBtn) {
            const supplierId = viewBtn.getAttribute('data-id');
            if (!supplierId) {
                alert('未获取到供应商ID');
                return;
            }

            try {
                const res = await fetch(`/suppliers/detail/${supplierId}`);
                if (!res.ok) throw new Error('获取详情失败');
                const supplier = await res.json();

                // 填充详情数据
                detailFields.id.textContent = supplier.supplier_id;
                detailFields.status.textContent = supplier.status === 1 ? '正常' : '封禁中';
                detailFields.name.textContent = supplier.supplier_name || '无';
                detailFields.contact.textContent = supplier.contact || '无';
                detailFields.phone.textContent = supplier.phone || '无';
                detailFields.address.textContent = supplier.address || '无';
                detailFields.varieties.textContent = supplier.varieties || '无';
                detailFields.cooperation.textContent = supplier.cooperation_start_date || '无';
                detailFields.createTime.textContent = supplier.create_time
                    ? new Date(supplier.create_time).toLocaleString()
                    : '无';
                detailFields.updateTime.textContent = supplier.update_time
                    ? new Date(supplier.update_time).toLocaleString()
                    : '无';
                detailFields.orderCount.textContent = supplier.order_count || 0;

                viewModal.style.display = 'flex';
            } catch (err) {
                console.error('查看详情失败:', err);
                alert('加载详情失败，请重试');
            }
        }
    });

    // 关闭详情弹窗
    closeViewBtn.addEventListener('click', () => viewModal.style.display = 'none');
    viewModal.addEventListener('click', (e) => {
        if (e.target === viewModal) viewModal.style.display = 'none';
    });


    // ====================== 4. 编辑功能 ======================
    const editModal = document.getElementById('edit-modal');
    const closeEditBtn = editModal.querySelector('.edit-close');
    const editForm = document.getElementById('edit-form');
    const editFields = {
        id: document.getElementById('edit-id'),
        name: document.getElementById('edit-name'),
        contact: document.getElementById('edit-contact'),
        phone: document.getElementById('edit-phone'),
        address: document.getElementById('edit-address'),
        varieties: document.getElementById('edit-varieties'),
        cooperation: document.getElementById('edit-cooperation'),
        status: document.getElementById('edit-status')
    };

    // 事件委托：支持按钮内部元素点击
    supplierTableBody.addEventListener('click', async (e) => {
        const editBtn = e.target.closest('.edit-btn');
        if (editBtn) {
            const supplierId = editBtn.getAttribute('data-id');
            if (!supplierId) {
                alert('未获取到供应商ID');
                return;
            }

            try {
                const res = await fetch(`/suppliers/detail/${supplierId}`);
                const supplier = await res.json();

                // 填充编辑表单 - 只填充存在的字段
                if (editFields.id) editFields.id.value = supplier.supplier_id;
                if (editFields.name) editFields.name.value = supplier.supplier_name || '';
                if (editFields.contact) editFields.contact.value = supplier.contact || '';
                if (editFields.phone) editFields.phone.value = supplier.phone || '';
                if (editFields.address) editFields.address.value = supplier.address || '';
                if (editFields.varieties) editFields.varieties.value = supplier.varieties || '';
                if (editFields.cooperation) editFields.cooperation.value = supplier.cooperation_start_date || '';
                if (editFields.status) editFields.status.value = supplier.status || 1;

                editModal.style.display = 'flex';
            } catch (err) {
                console.error('加载编辑数据失败:', err);
                alert('加载数据失败，请重试');
            }
        }
    });

    // 关闭编辑弹窗
    if (closeEditBtn) {
    closeEditBtn.addEventListener('click', () => editModal.style.display = 'none');
    editModal.addEventListener('click', (e) => {
        if (e.target === editModal) editModal.style.display = 'none';
    });
    }

    // 提交编辑表单
    if (editForm) {
    editForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const formData = new FormData(editForm);
        const data = Object.fromEntries(formData.entries());

        try {
            const res = await fetch('/suppliers/update', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });
            const result = await res.json();

            if (result.success) {
                alert('修改成功');
                editModal.style.display = 'none';
                window.location.reload();
            } else {
                alert('修改失败: ' + (result.message || '未知错误'));
            }
        } catch (err) {
            console.error('保存修改失败:', err);
            alert('网络错误，请重试');
        }
    });
    }
    // ====================== 6. 商品上架功能 ======================
    const addProductModal = document.getElementById('add-product-modal');
    const addProductClose = document.querySelector('.add-product-close');
    const addProductForm = document.getElementById('add-product-form');
    const productSupplierIdInput = document.getElementById('product-supplier-id');

    // 点击"上架商品"按钮显示弹窗
    supplierTableBody.addEventListener('click', async (e) => {
        const addProductBtn = e.target.closest('.add-product-btn');
        if (addProductBtn) {
            const supplierId = addProductBtn.getAttribute('data-id');
            if (supplierId) {
                try {
                    // 获取供应商详情，检查状态
                    const res = await fetch(`/suppliers/detail/${supplierId}`);
                    if (!res.ok) throw new Error('获取供应商详情失败');
                    const supplier = await res.json();
                    // 检查供应商状态：1=正常，0=封禁中
                    if (supplier.status !== 1) {
                        alert('该供应商已被封禁，无法上架商品！');
                        return;
                    }
                    // 供应商状态正常，填充供应商ID到隐藏域
                    productSupplierIdInput.value = supplierId;
                    addProductModal.style.display = 'flex';
                } catch (err) {
                    console.error('检查供应商状态失败:', err);
                    alert('获取供应商信息失败，请重试');
                }
            } else {
                alert('未获取到供应商ID');
            }
        }
    });
    //关闭弹窗
    if (addProductClose) {
        addProductClose.addEventListener('click', function() {
            addProductModal.style.display = 'none';
        });
    }
    addProductModal.addEventListener('click', function(e) {
        if (e.target === addProductModal) {
            addProductModal.style.display = 'none';
        }
    });
    // 提交商品上架表单
    if (addProductForm) {
        addProductForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(addProductForm);
            // 基础数据校验
            if (!formData.get('varietyName')) {
                alert('请输入商品名称');
                return;
            }
            if (!formData.get('price') || isNaN(formData.get('price')) || parseFloat(formData.get('price')) < 0) {
                alert('请输入有效的价格');
                return;
            }
            if (!formData.get('stock') || isNaN(formData.get('stock')) || parseInt(formData.get('stock')) < 0) {
                alert('请输入有效的库存数量');
                return;
            }
            if (!formData.get('productImage').name) {
                alert('请选择商品图片');
                return;
            }
            try {
                const response = await fetch('/products/add', {
                    method: 'POST',
                    body: formData
                });
                // 检查响应状态
                if (!response.ok) {
                    const errorText = await response.text();
                    throw new Error(`HTTP error! status: ${response.status}, message: ${errorText}`);
                }
                const result = await response.json();
                if (result.success) {
                    alert('商品上架成功！');
                    addProductModal.style.display = 'none';
                    addProductForm.reset();
                    // 可选：刷新页面
                    window.location.reload();
                } else {
                    alert('上架失败：' + (result.message || '未知错误'));
                }
            } catch (error) {
                console.error('商品上架请求失败:', error);
                alert('上架失败: ' + error.message);
            }
        });
    }

    // ====================== 5. 添加供应商 ======================
    const addBtn = document.getElementById('add-supplier');
    const addModal = document.getElementById('add-modal');
    const addModalClose = document.getElementById('add-modal-close');
    const addForm = document.getElementById('add-form');

    if (addBtn) {
    addBtn.addEventListener('click', () => addModal.style.display = 'flex');}
    if (addModalClose) {
    addModalClose.addEventListener('click', (e) => {
        e.stopPropagation();
        addModal.style.display = 'none';
    });
    }

    if (addForm) {
    addForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const data = Object.fromEntries(new FormData(addForm).entries());

        try {
            const res = await fetch('/suppliers/add', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });
            const result = await res.json();

            if (result.success) {
                alert('添加申请已发送，请等待审核');
                addModal.style.display = 'none';
                window.location.reload();
            } else {
                alert('添加失败: ' + (result.message || '未知错误'));
            }
        } catch (err) {
            console.error('添加失败:', err);
            alert('网络错误，请重试');
        }
    });
    }
});
