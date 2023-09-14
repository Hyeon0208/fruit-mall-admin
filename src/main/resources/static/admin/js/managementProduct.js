// 상태, 분류, 검색 버튼, 현재 페이지 값 초기 세팅
let selectedStatus, selectedCategory, selectedSearch;
let currentPage= 1;
let pageSize = 5;

// 이전 페이지 버튼
$(document).on('click', '.prevBtn', () => {
    currentPage -= 1;
    updateProductList();
});

// 페이지 번호 버튼
$(document).on('click', '.numberBtn', (e) => {
    currentPage = parseInt($(e.currentTarget).attr("value"));
    updateProductList();
});

// 다음 페이지 버튼
$(document).on('click', '.nextBtn', () => {
    currentPage += 1;
    updateProductList();
});

// 수정 버튼
$(document).on("click", ".editBtn", (e) => {
    const btn = $(e.currentTarget);
    const productId = btn.val();
    const editUrl = `/admin/edit/product/${productId}`;
    window.location.href = editUrl;
});

// 모두 체크
$(document).on("click", "#allCheck", (e) => {
    // chk1 체크박스의 체크 여부에 따라 모든 체크박스 상태를 변경
    const isChecked = $(e.target).prop("checked");
    console.log(isChecked);
    $("input[type='checkbox']").prop("checked", isChecked);
});

// 단일 체크
$(document).on("click", ".pageChk", (e)=> {
    const isChecked = $(e.target).prop("checked");
    const tr = $(e.target).closest("tr");
    const productId = tr.find("td:eq(1)").text();
});

// 선택 판매 중지
$(document).on("click", ".selectStopSaleBtn", () => {
    const checkedItems = $("input[type='checkbox']:checked");

    checkedItems.each((index, checkbox) => {
        const tr = $(checkbox).closest("tr");
        tr.find(".stopSaleBtn").click();
    });
});

// 선택 상품 삭제
$(document).on("click", ".selectDeleteBtn", () => {
    const checkedItems = $("input[type='checkbox']:checked");

    checkedItems.each((index, checkbox) => {
        const tr = $(checkbox).closest("tr");
        const productId = tr.find("td:eq(1)").text();

        axios({
            method: "delete",
            url: `/admin/delete/${productId}`
        }).then(res => {
            if (res.data === "success") {
                $(".txt05 h5").html("해당 상품이 정상적으로 삭제되었습니다." + "<br><a href=/admin/product>확인</a>");
                showModal();
                $('.txt05 a').click(() => {
                    $('.txt05').hide();
                });
            }
        })
    });
});

// n개 보기 셀렉트 박스 클릭 이벤트
$(document).on("change", "#pageSizeSelect", () => {
    pageSize = $("#pageSizeSelect").val();
    updateProductList();
});

// 엑셀 다운로드 버튼 클릭 이벤트
$(document).on("click", "#excelDownloadBtn", () => {
    const data = $(".product-table").html();
    const blob = new Blob([data], {type: "application/vnd.ms-excel"});
    const link = document.createElement("a");
    link.href = window.URL.createObjectURL(blob);
    link.download = "판매상품내역.xlsx";
    link.click();
});

// 게시상태 버튼
$(document).on("click", ".statusBtn", (e) => {
    selectedStatus =  $(e.currentTarget).val();
    currentPage= 1;
    updateProductList();
});

// 카테고리 분류 버튼
$(document).on("click", ".categoryBtn", (e) => {
    selectedCategory =  $(e.currentTarget).val();
    currentPage= 1;
    updateProductList();
});

// 검색어 입력 버튼
$(document).on("click", "#searchBtn", () => {
    selectedSearch = $("#searchCond").val();
    currentPage= 1;
    updateProductList();
});

// 판매 중지 버튼 클릭 이벤트
$(document).on("click", ".stopSaleBtn", (e) => {
    const btn = $(e.currentTarget);
    const productId = btn.val();

    axios({
        method: "post",
        url: "/admin/salestop",
        data: {
            productId: productId,
            status: "판매중지"
        },
        dataType: "json",
        headers: {'Content-Type': 'application/json'}
    }).then(res => {
        const updatedTime = res.data
        btn.hide();
        const timeSpan = btn.closest('td').find(".updateTime");
        const formattedTime = updatedTime.substring(0, 10).replaceAll('-', '.');

        if (timeSpan.length === 0) {
            const newUpdateTime = $(`<span class="updateTime">${formattedTime}</span>`);
            btn.closest('td').append(newUpdateTime);
        } else {
            timeSpan.text(formattedTime);
            timeSpan.show();
        }

        const productStatus = btn.closest('tr').find('.product_Status');
        productStatus.text("판매중지");

        localStorage.setItem(productId, "판매중지");
        localStorage.setItem(`updatedTime-${productId}`, formattedTime);
    })
});

$(() => {
    // 상품 가격 천자리 구분 기호 표시
    $(".productPrice").each((i, e) => {
        const price = $(e).text();
        $(e).text(price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ','));
    });

    // 새로 고침 시에도 판매 중지 열의 시간 표시
    $(".stopSaleBtn").each((i, e) => {
        const btn = $(e);
        const productId = btn.val();
        const status = localStorage.getItem(productId);

        if (status === "판매중지") {
            btn.hide();
            const timeSpan = btn.next(".updateTime");
            const storedDate = localStorage.getItem(`updatedTime-${productId}`);
            if (storedDate) {
                timeSpan.text(storedDate);
            }
            timeSpan.show();
        }
    });
});


const getCategoryName = (categoryId) => {
    const categories = ["봄과일", "여름과일", "가을과일", "겨울과일"];
    return categories[categoryId - 1];
};

function updateProductList() {
    axios({
        method: "get",
        url: "/admin/searchfilter",
        params: {
            status: selectedStatus,
            category: selectedCategory,
            searchCond: selectedSearch,
            pageNum: currentPage,
            pageSize: pageSize
        }
    }).then((res) => {
        const products = res.data.productPageInfo.list;
        const pageInfo = res.data.productPageInfo;
        const status = res.data.status;
        const category = res.data.category;

        // 클릭한 버튼 색상 추가
        $('.statusBtn').each((i, e) => {
            const btn = $(e);
            btn.removeClass('active');

            if (status === "전체상태" && btn.val() === "전체상태") {
                btn.addClass('active');
            } else if (status === "판매중" && btn.val() === "판매중") {
                btn.addClass('active');
            } else if (status === "판매중지" && btn.val() === "판매중지") {
                btn.addClass('active');
            } else if (status === "품절" && btn.val() === "품절") {
                btn.addClass('active');
            }
        });

        $('.categoryBtn').each((i, e) => {
            const btn = $(e);
            btn.removeClass('active');

            if (category === "전체카테고리" && btn.val() === "전체카테고리") {
                btn.addClass('active');
            } else if (category === "봄과일" && btn.val() === "봄과일") {
                btn.addClass('active');
            } else if (category === "여름과일" && btn.val() === "여름과일") {
                btn.addClass('active');
            } else if (category === "가을과일" && btn.val() === "가을과일") {
                btn.addClass('active');
            } else if (category === "겨울과일" && btn.val() === "겨울과일") {
                btn.addClass('active');
            }
        });

        // .product-table 클래스 내부의 tbody 내부 내용 교체
        const tableRows = products.map((product) => {

            $(".margin .bold").text(pageInfo.total);

            let stopBtn = $(`<button class="stopSaleBtn" value="${product.productId}">중지</button>`);
            let updateTime = "";

            if (product.productSaleStatus === "판매중지") {
                stopBtn = $("");
                updateTime = $(`<span class="updateTime">${new Date(product.productUpdatedAt).toLocaleDateString("ko-KR", { year: 'numeric', month: '2-digit', day: '2-digit' }).replaceAll(". ", ".").substring(0, 10)}</span>`);
            }

            return $("<tr>")
                .append($("<td>").html(`<input type="checkbox">`))
                .append($("<td>").text(product.productId))
                .append($("<td>").addClass("product_Status").text(product.productSaleStatus))
                .append($("<td>").text(getCategoryName(product.categoryId)))
                .append($("<td>").text(product.productName))
                .append($("<td>").text((product.productPrice * (1 - (product.productDiscount / 100))).toLocaleString("ko-KR", { maximumFractionDigits: 0 }) + "원"))
                .append($("<td>").text(product.productDiscount > 0 ? `${product.productDiscount}%` : "-"))
                .append($("<td>").attr("name", "찜수").text("-"))
                .append($("<td>").attr("name", "결제횟수").text("-"))
                .append($("<td>").attr("name", "리뷰수").text("-"))
                .append($("<td>").text(new Date(product.productCreatedAt).toLocaleDateString("ko-KR", { year: 'numeric', month: '2-digit', day: '2-digit' }).replaceAll(". ", ".").substring(0, 10)))
                .append($("<td>").html(`<button class="editBtn" value="${product.productId}">수정</button>`))
                .append($("<td>").append(stopBtn).append(updateTime));
        });
        $(".product-table tbody").empty().append(tableRows);

        // .pagination 클래스 태그 내부 내용 교체
        const paginationDiv = $('.pagination').empty();
        const numbersDiv = $('<p>').addClass('numbers');

        const totalData = pageInfo.pages; // 총 데이터 수
        const pageNumberList = pageInfo.navigatepageNums; // 페이지 번호들의 순서를 담은 배열
        const currentPage = pageInfo.pageNum;

        // 이전 페이지 버튼
        if (pageInfo.hasPreviousPage) {
            const prevBtn = $('<a>')
                .attr('href', '#')
                .addClass('prevBtn')
                .attr('value', pageInfo.prePage)
                .html('<span class="material-symbols-outlined">chevron_left</span>');
            paginationDiv.append(prevBtn);
        }

        // 페이지 번호 버튼
        pageNumberList.forEach((pageNumber) => {
            if (pageNumber <= totalData) {
                const numberBtn = $('<a>')
                    .text(pageNumber)
                    .attr('href', '#')
                    .addClass('numberBtn')
                    .attr('value', pageNumber);

                if (pageNumber === currentPage) {
                    numberBtn.addClass('bold').addClass("large-text").css("font-size", "16px");
                }
                numbersDiv.append(numberBtn);
            }
        });
        paginationDiv.append(numbersDiv);

        // 다음 페이지 버튼
        if (pageInfo.hasNextPage) {
            const nextBtn = $('<a>')
                .attr('href', '#')
                .addClass('nextBtn')
                .attr('value', pageInfo.nextPage)
                .html('<span class="material-symbols-outlined">chevron_right</span>');
            paginationDiv.append(nextBtn);
        }
    })
}

