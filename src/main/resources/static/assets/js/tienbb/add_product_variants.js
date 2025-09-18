// let variantIndex = $('#variant-group-list .variant-item').length;
//
// function createVariantItem(colorId, colorName, sizeId, sizeName, quantity = 0, price = 0) {
//     return `
// <div class="variant-item row bg-light p-3 rounded-3 shadow-sm mb-2 mx-1 align-items-center">
//     <div class="col-md-3 mb-2 mb-md-0">
//         <input type="text" class="form-control" value="${sizeName}" disabled>
//         <input type="hidden" name="variants[${variantIndex}].sizeId" value="${sizeId}">
//     </div>
//     <div class="col-md-3 mb-2 mb-md-0">
//         <input type="number" class="form-control" name="variants[${variantIndex}].quantity" value="${quantity}">
//     </div>
//     <div class="col-md-3 mb-2 mb-md-0">
//         <input type="number" class="form-control" name="variants[${variantIndex}].price" value="${price}">
//     </div>
//     <div class="col-md-3 text-end">
//         <button type="button" class="btn btn-outline-danger w-100 remove-variant">
//             <i class="bi bi-trash"></i> Xóa
//         </button>
//     </div>
//     <input type="hidden" name="variants[${variantIndex}].colorId" value="${colorId}">
// </div>`;
// }
//
// function createColorBlock(colorId, colorName, colorCode) {
//     return `
// <div class="card shadow rounded-3 mb-4 color-block" data-color="${colorId}">
//     <div class="card-header text-white d-flex justify-content-between align-items-center rounded-top"
//          data-bs-toggle="collapse" data-bs-target="#color-body-${colorId}" style="cursor: pointer; background-color: ${colorCode};">
//         <strong>${colorName}</strong>
//         <i class="bi bi-chevron-down"></i>
//     </div>
//     <div class="collapse show" id="color-body-${colorId}">
//         <div class="p-3">
//             <div class="variant-item row bg-light p-3 rounded-3 shadow-sm mb-2 mx-1 align-items-center">
//                 <div class="col-md-3 mb-2 mb-md-0"></div>
//                 <div class="col-md-3 mb-2 mb-md-0">
//                     <input type="number" class="form-control quantity-input" placeholder="Nhập số lượng chung" data-color-id="${colorId}">
//                 </div>
//                 <div class="col-md-3 mb-2 mb-md-0">
//                     <input type="number" class="form-control price-input" placeholder="Nhập giá chung" data-color-id="${colorId}">
//                 </div>
//                 <div class="col-md-3 text-end">
//                     <button type="button" class="btn btn-warning w-100 apply-price-quantity" data-color-id="${colorId}">
//                         Áp dụng
//                     </button>
//                 </div>
//             </div>
//
//             <div class="variant-list-inner"></div>
//
//             <div class="form-group mt-3">
//                 <input type="file" name="colorImages[${colorId}]" class="form-control multi-image-input" multiple accept="image/*">
//                 <div class="selected-images mt-2 d-flex flex-wrap gap-2" id="preview-color-${colorId}"></div>
//             </div>
//         </div>
//     </div>
// </div>`;
// }
//
// function updateVariantIndexes() {
//     variantIndex = 0;
//     $('#variant-group-list .variant-item').each(function () {
//         $(this).find('input[name]').each(function () {
//             const name = $(this).attr('name');
//             const newName = name.replace(/\[\d+]/, `[${variantIndex}]`);
//             $(this).attr('name', newName);
//         });
//         variantIndex++;
//     });
// }
//
// $(document).ready(function () {
//     $('#multi_color').select2({
//         placeholder: 'Chọn màu sắc',
//         templateResult: formatColor,
//         templateSelection: formatColor
//     });
//     $('#multi_size').select2({placeholder: 'Chọn kích cỡ'});
//
//     function formatColor(state) {
//         if (!state.id) return state.text;
//         const colorCode = $(state.element).data('color');
//         if (!colorCode) return state.text;
//
//         return $(`
//             <span>
//                 <span style="display: inline-block; width: 15px; height: 15px; background-color: ${colorCode}; border-radius: 50%; border: 1px solid #ccc; margin-right: 8px;"></span>
//                 ${state.text}
//             </span>
//         `);
//     }
//
//     $('#add-variant-btn').on('click', function () {
//         const selectedColors = $('#multi_color').val();
//         const selectedSizes = $('#multi_size').val();
//
//         if (!selectedColors || !selectedSizes) {
//             Swal.fire({
//                 icon: 'warning',
//                 title: 'Vui lòng chọn màu và kích cỡ!',
//                 toast: true,
//                 position: 'top-end',
//                 timer: 2000,
//                 showConfirmButton: false
//             });
//             return;
//         }
//
//         selectedColors.forEach(colorId => {
//             const colorName = $(`#multi_color option[value="${colorId}"]`).text();
//             const colorCode = $(`#multi_color option[value="${colorId}"]`).data('color');
//
//             let colorBlock = $(`.color-block[data-color="${colorId}"]`);
//
//             if (colorBlock.length === 0) {
//                 const htmlBlock = createColorBlock(colorId, colorName, colorCode);
//                 $('#variant-group-list').append(htmlBlock);
//                 colorBlock = $(`.color-block[data-color="${colorId}"]`);
//             }
//
//             const variantListInner = colorBlock.find('.variant-list-inner');
//
//             selectedSizes.forEach(sizeId => {
//                 const sizeName = $(`#multi_size option[value="${sizeId}"]`).text();
//                 const exists = variantListInner.find(`input[value="${sizeId}"][name$=".sizeId"]`).length > 0;
//                 if (exists) return;
//
//                 $.post('/admin/product/add2', {
//                     colorId: colorId,
//                     sizeId: sizeId
//                 }, function (response) {
//                     if (response === 'success') {
//                         const html = createVariantItem(colorId, colorName, sizeId, sizeName);
//                         variantListInner.append(html);
//                         updateVariantIndexes();
//                     } else {
//                         Swal.fire("Thông báo", response, "info");
//                     }
//                 });
//             });
//         });
//     });
//
//     $(document).on('click', '.remove-variant', function () {
//         const variantItem = $(this).closest('.variant-item');
//         const colorId = variantItem.find('input[name$=".colorId"]').val();
//         const sizeId = variantItem.find('input[name$=".sizeId"]').val();
//
//         $.post('/admin/product/remove-variant', {
//             colorId: colorId,
//             sizeId: sizeId
//         }, function (response) {
//             if (response === 'success') {
//                 variantItem.remove();
//
//                 const variantContainer = variantItem.closest('.variant-list-inner');
//                 if (variantContainer.children('.variant-item').length === 0) {
//                     variantContainer.closest('.color-block').remove();
//                 }
//
//                 updateVariantIndexes();
//             } else {
//                 Swal.fire({
//                     icon: 'error',
//                     title: 'Lỗi',
//                     toast: true,
//                     position: 'top-end',
//                     timer: 1500,
//                     showConfirmButton: false
//                 });
//             }
//         });
//     });
//
//     $(document).on('click', '.apply-price-quantity', function () {
//         const colorId = $(this).data('color-id');
//         const colorBlock = $(`.color-block[data-color="${colorId}"]`);
//         const quantity = parseInt(colorBlock.find('.quantity-input').val());
//         const price = parseInt(colorBlock.find('.price-input').val());
//
//         if (isNaN(quantity) || isNaN(price) || quantity < 0 || price < 0) {
//             Swal.fire({
//                 icon: 'warning',
//                 title: 'Số lượng và giá phải >= 0',
//                 toast: true,
//                 position: 'top-end',
//                 timer: 2000,
//                 showConfirmButton: false
//             });
//             return;
//         }
//
//         $.post('/admin/product/apply-price', {
//             colorId: colorId,
//             quantity: quantity,
//             price: price
//         }, function (response) {
//             if (response === 'success') {
//                 colorBlock.find('.variant-item').each(function () {
//                     $(this).find('input[name$=".quantity"]').val(quantity);
//                     $(this).find('input[name$=".price"]').val(price);
//                 });
//
//                 Swal.fire({
//                     icon: 'success',
//                     title: 'Đã áp dụng thành công',
//                     toast: true,
//                     position: 'top-end',
//                     timer: 1500,
//                     showConfirmButton: false
//                 });
//             } else {
//                 Swal.fire({
//                     icon: 'error',
//                     title: 'Lỗi',
//                     toast: true,
//                     position: 'top-end',
//                     timer: 1500,
//                     showConfirmButton: false
//                 });
//             }
//         });
//     });
// })