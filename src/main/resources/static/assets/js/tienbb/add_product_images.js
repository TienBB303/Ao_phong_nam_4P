// $(document).on("change", ".multi-image-input", function () {
//     const input = this;
//     const name = input.name;
//     const colorIdMatch = name.match(/\[(\d+)]/);
//     if (!colorIdMatch) return;
//
//     const colorId = colorIdMatch[1];
//     const previewContainer = $(`#preview-color-${colorId}`);
//     previewContainer.empty();
//
//     const files = input.files;
//     const maxFiles = 5;
//
//     if (files.length > maxFiles) {
//         Swal.fire({
//             icon: 'warning',
//             title: `Chỉ được chọn tối đa ${maxFiles} ảnh!`,
//             toast: true,
//             position: 'top-end',
//             timer: 2000,
//             showConfirmButton: false
//         });
//
//         input.value = "";
//         return;
//     }
//
//     Array.from(files).forEach(file => {
//         const reader = new FileReader();
//         reader.onload = function (e) {
//             const img = $('<img>').attr('src', e.target.result)
//                 .addClass('rounded border')
//                 .css({ width: '80px', height: '80px', objectFit: 'cover' });
//             previewContainer.append(img);
//         };
//         reader.readAsDataURL(file);
//     });
// });