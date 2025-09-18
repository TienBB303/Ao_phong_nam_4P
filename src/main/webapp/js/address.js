var list = []

async function loadAddress() {
    var urladd = 'http://localhost:8080/api/shipping/public/province';
    const response = await fetch(urladd, {
        method: 'GET'
    });
    list = await response.json();
    console.log(list)
    list = list.data
    var main = ``
    for (i = 0; i < list.length; i++) {
        main += `<option name-add="${list[i].ProvinceID}" value="${list[i].ProvinceName}">${list[i].ProvinceName}</option>`
    }
    document.getElementById("tinh").innerHTML = main
    loadHuyen(list[0].ProvinceID)
}

async function loadHuyen(idtinh) {
    var urladd = 'http://localhost:8080/api/shipping/public/district?provinceId='+idtinh;
    const response = await fetch(urladd, {
        method: 'GET'
    });
    list = await response.json();
    list = list.data
    var main = ``
    for (i = 0; i < list.length; i++) {
        main += `<option name-add="${list[i].DistrictID}" value="${list[i].DistrictName}">${list[i].DistrictName}</option>`
    }
    document.getElementById("huyen").innerHTML = main
    loadXa(list[0].DistrictID)
}

async function loadXa(idHuyen) {
    var urladd = 'http://localhost:8080/api/shipping/public/wards?districtId='+idHuyen;
    const response = await fetch(urladd, {
        method: 'GET'
    });
    list = await response.json();
    list = list.data
    console.log(list);

    var main = ``
    for (i = 0; i < list.length; i++) {
        main += `<option name-add="${list[i].WardCode}" value="${list[i].WardName}">${list[i].WardName}</option>`
    }
    document.getElementById("xa").innerHTML = main
}
