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
                                <div class="text-muted small">Barcode: ${item.barcodes}</div>
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

// tăng so luong san pham trong gio
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
// nút - / +
$('#product-in-cart').on('click', '.btn-qty-minus, .btn-qty-plus', function () {
    const isPlus = $(this).hasClass('btn-qty-plus');
    const id = $(this).data('id');

    // Tìm đúng input cùng hàng
    const $input = $(this)
        .closest('.input-group')
        .find('.update-quantity[data-id="' + id + '"]');

    let val = parseInt($input.val(), 10) || 0;
    val = isPlus ? val + 1 : val - 1;

    // if (val < 1) {
    //     Swal.fire({
    //         toast: true,
    //         icon: 'warning',
    //         title: 'Số lượng phải lớn hơn 1',
    //         position: 'top-end',
    //         showConfirmButton: false,
    //         timer: 700
    //     });
    //     return;
    // }

    // Gán lại và gọi change -> dùng lại hàm update-quantity sẵn có
    $input.val(val).trigger('change');
});

// (tuỳ chọn) Enter để áp số lượng mới
$('#product-in-cart').on('keydown', '.update-quantity', function (e) {
    if (e.key === 'Enter') $(this).trigger('change');
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
        title: 'Xóa hóa đơn?',
        text: 'Toàn bộ sản phẩm sẽ bị xóa khỏi hóa đơn!',
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

// xoas ma giam gia trong gio
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

function toggleRemoveButton() {
    const selected = $('#discount_select').val();
    if (!selected) {
        $('#remove_discount').hide();
    } else {
        $('#remove_discount').show();
    }
}

// Giao hàng
document.addEventListener("DOMContentLoaded", function () {
    const toggleSwitch = document.getElementById("toggleDeliveryInfoSwitch");
    const collapseDiv = document.getElementById("deliveryInfo");
    const inputFields = ["nameD", "phoneD", "addressD", "feeD"];
    const cartId = idCartFromPage;

    function formatVND(value) {
        if (!value) return "";
        return Number(value).toLocaleString("vi-VN") + " đ";
    }

    function parseCurrency(value) {
        return parseInt(value.replace(/[^\d]/g, ""), 10) || 0;
    }

    const feeInput = document.getElementById("feeD");
    if (feeInput && feeInput.value) {
        feeInput.value = formatVND(feeInput.value);
    }

    toggleSwitch.addEventListener("change", function () {
        const isDelivery = toggleSwitch.checked;
        const bsCollapse = new bootstrap.Collapse(collapseDiv, {
            toggle: false
        });

        if (isDelivery) {
            bsCollapse.show();
        } else {
            bsCollapse.hide();
        }

        sendDeliveryInfo(cartId, isDelivery);
    });

    inputFields.forEach(id => {
        const input = document.getElementById(id);
        if (!input) return;

        // Nếu là feeD thì cần xử lý riêng khi blur để format lại
        if (id === "feeD") {
            input.addEventListener("blur", function () {
                const raw = parseCurrency(input.value);
                input.value = formatVND(raw);
                sendDeliveryInfo(cartId, toggleSwitch.checked);
            });

            input.addEventListener("focus", function () {
                input.value = parseCurrency(input.value);
            });

            input.addEventListener("keydown", function (e) {
                if (e.key === "Enter") {
                    input.blur();
                }
            });
        } else {
            // Các input còn lại thì gửi khi thay đổi
            input.addEventListener("input", function () {
                if (toggleSwitch.checked) {
                    sendDeliveryInfo(cartId, true);
                }
            });
        }
    });
    function sendDeliveryInfo(cartId, isDelivery) {
        const nameD = isDelivery ? document.getElementById("nameD").value : "";
        const phoneD = isDelivery ? document.getElementById("phoneD").value : "";
        const addressD = isDelivery ? document.getElementById("addressD").value : "";
        const feeD = isDelivery ? parseCurrency(document.getElementById("feeD").value) : 0;         // format String về dạng bigdecimal

        $.ajax({
            url: "/admin/sell-inline/delivery",
            type: "POST",
            data: {
                cartId: cartId,
                isDelivery: isDelivery,
                nameD: nameD,
                phoneD: phoneD,
                addressD: addressD,
                feeD: feeD
            },
            success: function (res) {
                // Cập nhật lại trường tổng thanh toán
                const formattedTotal = new Intl.NumberFormat('vi-VN').format(res.totalCheckout) + ' ₫';
                $('#total_price_checkout').val(formattedTotal);

            },
            error: function (xhr) {
                console.error("Lỗi:", xhr.responseText);

                Swal.fire({
                    icon: 'error',
                    title: xhr.responseText || 'Có lỗi xảy ra khi cập nhật phí giao hàng',
                    toast: true,
                    position: 'top-end',
                    timer: 1500,
                    showConfirmButton: false
                });
            }
        });
    }
});
//Tìm khách hàng
$('#customer_search').on('input', function () {
    const keyword = $(this).val().trim();

    if (keyword.length === 0) {
        // Trường input trống -> gọi API xóa khách khỏi giỏ hàng
        $.ajax({
            url: '/admin/sell-inline/remove-customer-from-cart',
            method: 'POST',
            data: {
                cartId: idCartFromPage
            },
            success: function () {
                console.log("Đã xóa khách khỏi cart");
            }
        });

        $("#suggestionBox_Customer").hide();
        return;
    }

    // Nếu có ký tự -> tìm kiếm khách hàng
    $.ajax({
        url: '/admin/sell-inline/search-customer-inline',
        method: 'GET',
        data: { keyword: keyword },
        success: function (data) {
            let html = '';
            data.forEach(customer => {
                html += `
                    <div class="p-2 suggestion-item border-bottom" 
                         data-id="${customer.id}"
                         data-name="${customer.name + " - "+ customer.phoneNumber}" 
                         data-nameD="${customer.name}" 
                         data-phoneD="${customer.phoneNumber}">
                        <div class="fw-semibold">Họ tên: ${customer.name}</div>
                        <div class="text-muted small">SĐT: ${customer.phoneNumber}</div>
                    </div>
                `;
            });
            $('#suggestionBox_Customer').html(html).show();
        }
    });
});
// gắn khách hàng vào cart
$('#suggestionBox_Customer').on('click', '.suggestion-item', function () {
    const customerId = $(this).data('id');
    const customerName = $(this).data('name');

    $('#customer_search').val(customerName);
    $('#customerId').val(customerId); // lưu lại ID nếu cần submit form
    $('#suggestionBox_Customer').hide();

    $.ajax({
        url: '/admin/sell-inline/add-customer-to-cart',
        method: 'POST',
        data: {
            idCart: idCartFromPage,
            customerId: customerId
        },
        success: function (data) {
            console.log("Đã gán khách vào cart");

            $('#nameD').val(data.name);
            $('#phoneD').val(data.phone);
            $('#addressD').val(data.address); // gán địa chỉ xuống input giao hàng
        },
        error: function () {
            alert("Không thể thêm khách hàng vào giỏ hàng.");
        }
    });
});
//Thêm khách hàng
$(document).ready(function () {
    // Gắn sự kiện submit cho nút "Lưu"
    $('#addCustomerModal .btn-primary').click(function () {
        const name = $('#customer_name').val().trim();
        const phone = $('#customer_phone').val().trim();

        if (name === '' || phone === '') {
            Swal.fire({
                icon: 'warning',
                title: 'Thiếu thông tin',
                text: 'Vui lòng nhập đầy đủ tên và số điện thoại!',
                toast: true,
                position: 'top-end',
                timer: 1000,
                showConfirmButton: false
            });
            return;
        }

        $.ajax({
            url: '/admin/sell-inline/add-customer',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({
                name: name,
                phoneNumber: phone
            }),
            success: function (response) {
                Swal.fire({
                    icon: 'success',
                    title: 'Đã thêm khách hàng!',
                    toast: true,
                    position: 'top-end',
                    timer: 1000,
                    showConfirmButton: false
                });

                const modalEl = document.getElementById('addCustomerModal');
                const modalInstance = bootstrap.Modal.getInstance(modalEl);
                modalInstance.hide();

                // Remove backdrop nếu còn sót
                $('.modal-backdrop').remove();
                $('body').removeClass('modal-open');

                // Reset form
                $('#addCustomerForm')[0].reset();

                // Ẩn modal
                $('#addCustomerModal').modal('hide');
                $.ajax({
                    url: '/admin/sell-inline/add-customer-to-cart',
                    method: 'POST',
                    data: {
                        idCart: idCartFromPage,
                        customerId: response.id
                    },
                    success: function () {
                        console.log("Khách mới đã được gắn vào cart");

                        // Gán tên khách vào input hiển thị
                        $('#customer_search').val(response.name);
                        $('#customerId').val(response.id);

                        setTimeout(function () {
                            location.reload();
                        }, 1000);
                    }
                });
            },
            error: function (xhr) {
                Swal.fire({
                    icon: 'error',
                    title: 'Lỗi',
                    text: xhr.responseText || 'Đã có lỗi xảy ra',
                    toast: true,
                    position: 'top-end',
                    timer: 1000,
                    showConfirmButton: false
                });
            }
        });
    });
});

// hàm bật camera và quét thêm sản phẩm
(function () {
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else init();

    function init() {
        const cameraModal = document.getElementById('cameraModal');
        const scannerBox  = document.getElementById('barcode-scanner');
        const resultBox   = document.getElementById('barcode-result');

        // Nếu chưa có modal/scanner thì thôi (nhưng KHÔNG kiểm tra các phần tử khác)
        if (!cameraModal || !scannerBox || !resultBox) {
            console.warn('POS scanner: thiếu modal hoặc scanner box');
            return;
        }

        let quaggaOn = false;
        let locking  = false;
        let lastCode = null, lastTime = 0;

        // Delegation: click vào span hoặc icon bên trong đều bắt được
        document.addEventListener('click', function (e) {
            const trigger = e.target.closest('#productSearchBarcode');
            if (trigger) {
                e.preventDefault();
                openCamera();
            }
            if (e.target.closest('#closeCameraBtn')) {
                e.preventDefault();
                closeCamera();
            }
        });

        // Hỗ trợ Enter/Space khi focus vào trigger
        document.addEventListener('keydown', function (e) {
            if (e.target.id === 'productSearchBarcode' && (e.key === 'Enter' || e.key === ' ')) {
                e.preventDefault();
                openCamera();
            }
        });

        function openCamera() {
            cameraModal.classList.remove('d-none');
            document.body.style.overflow = 'hidden';
            startQuagga();
        }
        function closeCamera() {
            stopQuagga();
            cameraModal.classList.add('d-none');
            document.body.style.overflow = '';
            resultBox.textContent = 'Đưa mã vạch vào khung hình';
            locking = false;
        }

        function startQuagga() {
            if (quaggaOn || !window.Quagga) {
                if (!window.Quagga) console.warn('Quagga chưa nạp!');
                return;
            }
            Quagga.init({
                inputStream: {
                    type: "LiveStream",
                    target: scannerBox,
                    constraints: { facingMode: "environment", width: {ideal: 640}, height: {ideal: 480} }
                },
                decoder: { readers: ["ean_reader","ean_8_reader","code_128_reader","code_39_reader"] },
                locate: true,
                numOfWorkers: navigator.hardwareConcurrency ? Math.min(4, navigator.hardwareConcurrency) : 2
            }, (err) => {
                if (err) {
                    console.error(err);
                    resultBox.textContent = 'Không mở được camera. Kiểm tra quyền.';
                    return;
                }
                Quagga.start();
                quaggaOn = true;
                resultBox.textContent = 'Đưa mã vạch vào khung hình';
            });

            Quagga.offDetected(onDetected);
            Quagga.onDetected(onDetected);
        }

        function stopQuagga() {
            try { Quagga.offDetected(onDetected); } catch(e){}
            if (quaggaOn) { Quagga.stop(); quaggaOn = false; }
            const video = scannerBox.querySelector('video');
            const tracks = video?.srcObject?.getTracks?.() || [];
            tracks.forEach(t => t.stop());
        }

        function onDetected(data) {
            const code = (data?.codeResult?.code || '').trim();
            if (!code) return;

            const now = Date.now();
            if (code === lastCode && (now - lastTime) < 1200) return;
            lastCode = code; lastTime = now;

            if (locking) return;
            locking = true;

            resultBox.textContent = `Đang thêm: ${code} ...`;

            // >>> Chỉ kiểm tra idCartFromPage TẠI ĐÂY <<<
            const idCartFromPage = window.idCartFromPage;
            if (!idCartFromPage && idCartFromPage !== 0) {
                resultBox.textContent = 'Thiếu ID giỏ hàng';
                setTimeout(() => { locking = false; }, 900);
                return;
            }

            $.ajax({
                url: '/admin/sell-inline/add-by-barcode',
                method: 'POST',
                data: { idCart: idCartFromPage, barcode: code },
                // headers: { 'X-CSRF-TOKEN': $('meta[name="_csrf"]').attr('content') }
            }).done(function () {
                if (window.Swal) {
                    Swal.fire({toast:true, icon:'success', title:`Đã thêm: ${code}`, position:'top-end', showConfirmButton:false, timer:700});
                }
                resultBox.textContent = `Đã thêm sản phẩm (${code})`;
                setTimeout(() => { closeCamera(); location.reload(); }, 400);
            }).fail(function (xhr) {
                const msg = xhr?.responseText || 'Thêm thất bại';
                if (window.Swal) {
                    Swal.fire({toast:true, icon:'error', title: msg, position:'top-end', showConfirmButton:false, timer:1300});
                }
                resultBox.textContent = msg;
                setTimeout(() => { locking = false; }, 900);
            });
        }

        // Expose cho debug
        window.__openPosCamera = openCamera;
        window.__closePosCamera = closeCamera;
    }
})();


