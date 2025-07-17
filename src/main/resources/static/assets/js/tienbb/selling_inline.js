// tao gio hang moi
$('#taoCartMoi').on('submit', function (e) {
    e.preventDefault(); // Ngăn form thực hiện submit mặc định

    $.ajax({
        url: '/admin/sell-inline/create-cart',
        type: 'POST',
        success: function (response) {
            Swal.fire({
                toast: true,
                icon: 'success',
                title: response, // Thông báo thành công từ server
                position: 'top-end',
                showConfirmButton: false,
                timer: 500,
                timerProgressBar: true
            }).then(() => {
                location.reload();
            });
        },
        error: function (xhr) {
            Swal.fire({
                toast: true,
                icon: 'error',
                title: xhr.responseText, // Thông báo lỗi trả về từ controller
                position: 'top-end',
                showConfirmButton: false,
                timer: 2000,
                timerProgressBar: true
            }).then(() => {
                location.reload();
            });
        }
    });
});

// tim kiem san pham
$('#productSearchName').on('input', function () {
    const keyword = $(this).val();
    if (keyword.length < 1) {
        $('#suggestionBox').hide();
        return;
    }

    $.ajax({
        url: '/admin/sell-inline/search-product-detail',
        method: 'GET',
        data: { keyword: keyword },
        success: function (data) {
            let html = '';
            data.forEach(item => {
                html += `
                        <div class="p-2 suggestion-item border-bottom d-flex justify-content-between align-items-center" 
                        data-id="${item.id}"
                        data-name="${item.displayName}"
                        data-price="${item.price}">
                            <div>
                                <div class="fw-semibold">${item.displayName}</div>
                                <div class="text-muted small">Barcode: ${item.barcode}</div>
                                <div class="text-muted small">Tồn kho: ${item.quantity}</div>
                            </div>
                            <div class="fw-bold text-success">${item.price.toLocaleString()} đ</div>
                        </div>`;
            });
            $('#suggestionBox').html(html).show();
        }
    });
});

// san pham sau khi tim kiem, an de them vao gio
$('#suggestionBox').on('click', '.suggestion-item', function () {
    const id = $(this).data('id');
    // Gửi request thêm vào cart
    $.ajax({
        url: '/admin/sell-inline/add-to-cart',
        method: 'POST',
        data: {
            idCart: idCartFromPage, // idCart từ giao diện vào biến JS
            productDetailId: id
        },
        success: function () {
            Swal.fire({
                toast: true,
                icon: 'success',
                title: 'Đã thêm vào giỏ',
                position: 'top-end',
                showConfirmButton: false,
                timer: 500,
                timerProgressBar: true
            }).then(() => {
                location.reload();
            });
            // location.reload(); // Hoặc gọi ajax để load lại list cart detail
        }
    });
    $('#suggestionBox').hide();
    $('#productSearchName').val('');
});

// tanwg so luong san pham trong gio
$('#product-in-cart').on('change', '.update-quantity', function (){
    const $input = $(this);
    const cartDetailId = $input.data('id');
    const newQuantity = $input.val();
    const oldQuantity = $input.data('old'); // map lai gia tri cu neu nhu nhap qua so luong
    if(newQuantity < 1){
        Swal.fire({
            toast: true,
            icon: 'warning',
            title: 'Số lượng phải lớn hơn 1',
            position: 'top-end',
            showConfirmButton: false,
            timer: 500,
            timerProgressBar: true
        });
        $input.val(oldQuantity); //gan lai vao o inpout
        return;
    }
    $.ajax({
        url: '/admin/sell-inline/update-quantity',
        type: 'POST',
        data: {
            cartDetailId: cartDetailId,
            quantity: newQuantity
        },
        success: function (response) {
            Swal.fire({
                toast: true,
                icon: 'success',
                title: response,
                position: 'top-end',
                showConfirmButton: false,
                timer: 500,
                timerProgressBar: true
            }).then(() => {
                location.reload();
            });
        },
        error: function (xhr) {
            Swal.fire({
                icon: 'error',
                title: xhr.responseText,
                toast: true,
                position: 'top-end',
                showConfirmButton: false,
                timer: 1500,
                timerProgressBar: true
            });
            $input.val(oldQuantity);
        }
    });
});

//Delete Item in cart
$('#product-in-cart').on('click', '.delete-item', function () {
    const cartDetailId = $(this).data('id');

    Swal.fire({
        icon: 'warning',
        title: 'Xác nhận xóa?',
        showCancelButton: true,
        confirmButtonText: 'Xóa',
        cancelButtonText: 'Hủy'
    }).then((result) => {
        if (result.isConfirmed) {
            $.ajax({
                url: '/admin/sell-inline/delete-item',
                type: 'DELETE',
                data: { cartDetailId: cartDetailId },
                success: function (response) {
                    Swal.fire({
                        toast: true,
                        icon: 'success',
                        title: response,
                        position: 'top-end',
                        showConfirmButton: false,
                        timer: 1000,
                        timerProgressBar: true
                    }).then(() => {
                        location.reload();
                    });
                },
                error: function (xhr) {
                    Swal.fire({
                        icon: 'error',
                        title: xhr.responseText,
                        toast: true,
                        position: 'top-end',
                        showConfirmButton: false,
                        timer: 1500
                    });
                }
            });
        }
    });
});

//Delete cart
$(document).on('click', '.delete-cart', function (){
    const idCart = $(this).data("id")
    Swal.fire({
        icon: 'warning',
        title: 'Xóa giỏ hàng?',
        text: 'Toàn bộ sản phẩm sẽ bị xóa khỏi giỏ!',
        showCancelButton: true,
        confirmButtonText: 'Xóa',
        cancelButtonText: 'Hủy'
    }).then((result) => {
        if (result.isConfirmed) {
            $.ajax({
                url: '/admin/sell-inline/delete-cart',
                method: 'DELETE',
                data: { idCart: idCart },
                success: function (response) {
                    Swal.fire({
                        toast: true,
                        icon: 'success',
                        title: response,
                        position: 'top-end',
                        showConfirmButton: false,
                        timer: 1000
                    }).then(() => {
                        location.href = '/admin/sell-inline/hien-thi';
                    });
                },
                error: function (xhr) {
                    Swal.fire({
                        toast: true,
                        icon: 'error',
                        title: xhr.responseText,
                        position: 'top-end',
                        showConfirmButton: false,
                        timer: 1500
                    });
                }
            });
        }
    });
});

// thanh toán
$('#thanhToanBtn').click(function () {
    const idCart = $(this).data('id');
    const typePayment = $('#type_payment_select').val();

    Swal.fire({
        title: 'Xác nhận thanh toán?',
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: 'Thanh toán',
        cancelButtonText: 'Hủy',
    }).then((result) => {
        if (result.isConfirmed) {
            $.ajax({
                url: '/admin/sell-inline/thanh-toan',
                method: 'POST',
                data: { idCart: idCart , typePayment: typePayment },
                success: function (res) {
                    Swal.fire({
                        icon: 'success',
                        title: res,
                        toast: true,
                        position: 'top-end',
                        timer: 1000,
                        showConfirmButton: false
                    }).then(() => {
                        window.location.href = '/admin/sell-inline/hien-thi';// Hoặc chuyển sang trang in hóa đơn
                    });
                },
                error: function (err) {
                    Swal.fire({
                        icon: 'error',
                        title: err.responseText,
                        toast: true,
                        position: 'top-end',
                        timer: 1500,
                        showConfirmButton: false
                    });
                }
            });
        }
    });
});

//áp mã giảm giá cho cart
$('#discount_select').change(function () {
    const discountId = $(this).val();
    const idCart = $(this).data('id');

    if (!discountId) return;
    $.ajax({
        url: '/admin/sell-inline/apply-discount',
        method: 'POST',
        data: {
            idCart: idCart,
            discountId: discountId
        },
        success: function (res) {
            Swal.fire({
                icon: 'success',
                title: res,
                toast: true,
                position: 'top-end',
                timer: 1200,
                showConfirmButton: false
            }).then(() => {
                location.reload(); // Hoặc cập nhật block thanh toán
            });
        },
        error: function (err) {
            Swal.fire({
                icon: 'error',
                title: err.responseText,
                toast: true,
                position: 'top-end',
                timer: 1500,
                showConfirmButton: false
            });
        }
    });
});

$('#remove_discount').click(function () {
    const idCart = $(this).data('id');

    if(idCart != null){
        $.ajax({
            url: '/admin/sell-inline/remove-discount',
            method: 'POST',
            data: {
                idCart: idCart
            },
            success: function (res) {
                Swal.fire({
                    icon: 'info',
                    title: res,
                    toast: true,
                    position: 'top-end',
                    timer: 1200,
                    showConfirmButton: false
                }).then(() => {
                    location.reload();
                });
            },
            error: function (err) {
                Swal.fire({
                    icon: 'error',
                    title: err.responseText,
                    toast: true,
                    position: 'top-end',
                    timer: 1500,
                    showConfirmButton: false
                });
            }
        });
    }else{
        return;
    }
});

// function toggleRemoveButton() {
//     const selected = $('#discount_select').val();
//     if (!selected) {
//         $('#remove_discount').hide();
//     } else {
//         $('#remove_discount').show();
//     }
// }
// Tìm kiếm khách hàng
$('#customerSearchInput').on('input', function () {
    const keyword = $(this).val();
    if (keyword.length < 2) {
        $('#customerSuggestionBox').hide();
        return;
    }

    $.ajax({
        url: '/admin/sell-inline/search-customer',
        method: 'GET',
        data: { keyword: keyword },
        success: function (data) {
            let html = '';
            if (data.length > 0) {
                data.forEach(function (customer) {
                    html += '<div class="suggestion-item p-2 border-bottom cursor-pointer hover-bg-light" ' +
                        'data-customer-id="' + customer.id + '" ' +
                        'data-customer-name="' + customer.name + '" ' +
                        'data-customer-phone="' + customer.phoneNumber + '" ' +
                        'data-customer-email="' + (customer.email || 'Chưa có email') + '">' +
                        '<div class="fw-bold">' + customer.name + '</div>' +
                        '<small class="text-muted">SĐT: ' + customer.phoneNumber + ' | Email: ' + (customer.email || 'Chưa có email') + '</small>' +
                        '</div>';
                });
            } else {
                html = '<div class="p-2 text-muted">Không tìm thấy khách hàng nào</div>';
            }
            $('#customerSuggestionBox').html(html).show();
        },
        error: function (err) {
            console.error('Lỗi tìm kiếm khách hàng:', err);
            $('#customerSuggestionBox').hide();
        }
    });
});

// Chọn khách hàng từ danh sách gợi ý
$(document).on('click', '.suggestion-item', function () {
    const customerId = $(this).data('customer-id');
    const customerName = $(this).data('customer-name');
    const customerPhone = $(this).data('customer-phone');
    const customerEmail = $(this).data('customer-email');

    // Lưu thông tin khách hàng
    $('#selectedCustomerId').val(customerId);
    $('#customerSearchInput').val(customerName);

    // Hiển thị thông tin khách hàng đã chọn
    $('#customerDisplayName').text(customerName);
    $('#customerDisplayPhone').text(customerPhone);
    $('#customerDisplayEmail').text(customerEmail);
    $('#selectedCustomerInfo').show();

    // Ẩn danh sách gợi ý
    $('#customerSuggestionBox').hide();

    // Gọi API để gắn khách hàng vào cart hiện tại (nếu có)
    const currentCartId = idCartFromPage; // Biến global được set từ template
    if (currentCartId) {
        $.ajax({
            url: '/admin/sell-inline/assign-customer',
            method: 'POST',
            data: {
                cartId: currentCartId,
                customerId: customerId
            },
            success: function (response) {
                console.log('Gắn khách hàng thành công:', response);
            },
            error: function (err) {
                console.error('Lỗi gắn khách hàng:', err);
                Swal.fire({
                    toast: true,
                    icon: 'error',
                    title: 'Lỗi gắn khách hàng: ' + err.responseText,
                    position: 'top-end',
                    showConfirmButton: false,
                    timer: 2000,
                    timerProgressBar: true
                });
            }
        });
    }
});

// Xóa khách hàng đã chọn
$('#clearCustomerBtn').on('click', function () {
    const currentCartId = idCartFromPage;

    if (currentCartId) {
        $.ajax({
            url: '/admin/sell-inline/clear-customer',
            method: 'POST',
            data: { cartId: currentCartId },
            success: function (response) {
                console.log('Xóa khách hàng thành công:', response);
                // Clear UI
                $('#selectedCustomerId').val('');
                $('#customerSearchInput').val('');
                $('#selectedCustomerInfo').hide();
                $('#customerSuggestionBox').hide();

                Swal.fire({
                    toast: true,
                    icon: 'success',
                    title: 'Đã hủy chọn khách hàng',
                    position: 'top-end',
                    showConfirmButton: false,
                    timer: 1500,
                    timerProgressBar: true
                });
            },
            error: function (err) {
                console.error('Lỗi xóa khách hàng:', err);
                $('#selectedCustomerId').val('');
                $('#customerSearchInput').val('');
                $('#selectedCustomerInfo').hide();
                $('#customerSuggestionBox').hide();

                Swal.fire({
                    toast: true,
                    icon: 'warning',
                    title: 'Đã hủy chọn khách hàng (có thể có lỗi xóa khỏi session)',
                    position: 'top-end',
                    showConfirmButton: false,
                    timer: 2000,
                    timerProgressBar: true
                });
            }
        });
    } else {
        $('#selectedCustomerId').val('');
        $('#customerSearchInput').val('');
        $('#selectedCustomerInfo').hide();
        $('#customerSuggestionBox').hide();
    }
});

// Ẩn danh sách gợi ý khi click ra ngoài
$(document).on('click', function (e) {
    if (!$(e.target).closest('#customerSearchInput, #customerSuggestionBox').length) {
        $('#customerSuggestionBox').hide();
    }
});

// CSS cho hover effect
$('<style>').text(`
    .suggestion-item:hover, .hover-bg-light:hover {
        background-color: #f8f9fa !important;
        cursor: pointer;
    }
    .cursor-pointer {
        cursor: pointer;
    }
`).appendTo('head');