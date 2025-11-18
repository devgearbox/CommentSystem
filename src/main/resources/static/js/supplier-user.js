document.addEventListener('DOMContentLoaded', function () {
    // 弹窗元素
    const addModal = document.getElementById('add-modal');
    const editModal = document.getElementById('edit-modal');
    const addProductModal = document.getElementById('add-product-modal');

    // 按钮元素
    const addSupplierBtn = document.getElementById('add-supplier-btn');
    const emptyAddSupplierBtn = document.getElementById('empty-add-supplier-btn');
    const editSupplierBtn = document.getElementById('edit-supplier-btn');
    const addProductBtn = document.getElementById('add-product-btn');

    // 表单元素
    const addForm = document.getElementById('add-form');
    const editForm = document.getElementById('edit-form');
    const addProductForm = document.getElementById('add-product-form');

    // 关闭按钮
    const addModalClose = document.getElementById('add-modal-close');
    const editClose = document.querySelector('.edit-close');
    const addProductClose = document.querySelector('.add-product-close');

    // ====================== 添加供应商功能 ======================
    function showAddModal() {
        addModal.style.display = 'flex';
    }

    if (addSupplierBtn) {
        addSupplierBtn.addEventListener('click', showAddModal);
    }

    if (emptyAddSupplierBtn) {
        emptyAddSupplierBtn.addEventListener('click', showAddModal);
    }

    if (addModalClose) {
        addModalClose.addEventListener('click', () => addModal.style.display = 'none');
    }

    addModal.addEventListener('click', (e) => {
        if (e.target === addModal) addModal.style.display = 'none';
    });

    // 提交添加供应商表单
    if (addForm) {
        addForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(addForm);
            const data = Object.fromEntries(formData.entries());

            // 设置今天的日期作为默认合作开始日期（如果未填写）
            if (!data.cooperation_start_date) {
                const today = new Date().toISOString().split('T')[0];
                data.cooperation_start_date = today;
            }

            try {
                const response = await fetch('/suppliers/add', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(data)
                });

                const result = await response.json();

                if (result.success) {
                    alert('供应商申请已提交，请等待管理员审核');
                    addModal.style.display = 'none';
                    addForm.reset();
                    // 刷新页面显示新状态
                    window.location.reload();
                } else {
                    alert('提交失败: ' + (result.message || '未知错误'));
                }
            } catch (error) {
                console.error('添加供应商失败:', error);
                alert('网络错误，请重试');
            }
        });
    }

    // ====================== 编辑供应商功能 ======================
    if (editSupplierBtn) {
        editSupplierBtn.addEventListener('click', async () => {
            try {
                // 获取当前供应商信息
                const response = await fetch('/suppliers/detail/current');
                const supplier = await response.json();

                if (supplier) {
                    // 填充编辑表单
                    document.getElementById('edit-id').value = supplier.supplier_id;
                    document.getElementById('edit-name').value = supplier.supplier_name || '';
                    document.getElementById('edit-contact').value = supplier.contact || '';
                    document.getElementById('edit-phone').value = supplier.phone || '';
                    document.getElementById('edit-address').value = supplier.address || '';
                    document.getElementById('edit-varieties').value = supplier.varieties || '';

                    // 格式化日期
                    if (supplier.cooperation_start_date) {
                        const date = new Date(supplier.cooperation_start_date);
                        document.getElementById('edit-cooperation').value = date.toISOString().split('T')[0];
                    }

                    editModal.style.display = 'flex';
                }
            } catch (error) {
                console.error('加载供应商信息失败:', error);
                alert('加载信息失败，请重试');
            }
        });
    }

    if (editClose) {
        editClose.addEventListener('click', () => editModal.style.display = 'none');
    }

    editModal.addEventListener('click', (e) => {
        if (e.target === editModal) editModal.style.display = 'none';
    });

    // 提交编辑表单
    if (editForm) {
        editForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(editForm);
            const data = Object.fromEntries(formData.entries());

            try {
                const response = await fetch('/suppliers/update', {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(data)
                });

                const result = await response.json();

                if (result.success) {
                    alert('信息更新成功');
                    editModal.style.display = 'none';
                    window.location.reload();
                } else {
                    alert('更新失败: ' + (result.message || '未知错误'));
                }
            } catch (error) {
                console.error('更新供应商失败:', error);
                alert('网络错误，请重试');
            }
        });
    }

    // ====================== 上架商品功能 ======================
    if (addProductBtn) {
        addProductBtn.addEventListener('click', () => {
            // 设置当前供应商ID
            document.getElementById('product-supplier-id').value = getCurrentSupplierId();
            addProductModal.style.display = 'flex';
        });
    }

    if (addProductClose) {
        addProductClose.addEventListener('click', () => addProductModal.style.display = 'none');
    }

    addProductModal.addEventListener('click', (e) => {
        if (e.target === addProductModal) addProductModal.style.display = 'none';
    });

    // 提交商品上架表单
    if (addProductForm) {
        addProductForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(addProductForm);

            // 基础验证
            if (!formData.get('varietyName')) {
                alert('请输入商品名称');
                return;
            }

            if (!formData.get('price') || parseFloat(formData.get('price')) <= 0) {
                alert('请输入有效的价格');
                return;
            }

            if (!formData.get('stock') || parseInt(formData.get('stock')) < 0) {
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

                const result = await response.json();

                if (result.success) {
                    alert('商品上架成功！');
                    addProductModal.style.display = 'none';
                    addProductForm.reset();
                } else {
                    alert('上架失败: ' + (result.message || '未知错误'));
                }
            } catch (error) {
                console.error('上架商品失败:', error);
                alert('网络错误，请重试');
            }
        });
    }

    // 辅助函数：获取当前供应商ID（需要从页面中提取）
    function getCurrentSupplierId() {
        // 这里需要从页面中获取当前供应商的ID
        // 可以通过隐藏字段或其他方式传递
        const supplierIdElement = document.querySelector('[data-supplier-id]');
        return supplierIdElement ? supplierIdElement.dataset.supplierId : null;
    }
});