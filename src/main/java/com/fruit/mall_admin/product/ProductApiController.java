package com.fruit.mall_admin.product;

import com.fruit.mall_admin.category.CategoryService;
import com.fruit.mall_admin.firebase.FireBaseService;
import com.fruit.mall_admin.firebase.UploadResult;
import com.fruit.mall_admin.image.Image;
import com.fruit.mall_admin.image.ImageService;
import com.fruit.mall_admin.image.dto.FileInfo;
import com.fruit.mall_admin.product.dto.PageResDto;
import com.fruit.mall_admin.product.dto.ProductRegistrationForm;
import com.fruit.mall_admin.product.dto.ProductResDto;
import com.fruit.mall_admin.product.dto.SaleStopDto;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class ProductApiController {
    private final ProductService productService;
    private final ImageService imageService;
    private final FireBaseService fireBaseService;
    private final CategoryService categoryService;

    private final static String PATH = "images";
    private final static String ON_SALE = "판매중";

    @PostMapping("/add/product")
    @ResponseBody
    public String addProduct(@ModelAttribute ProductRegistrationForm form,
                             @RequestParam("images") List<MultipartFile> images,
                             @RequestParam(value = "imageUrls", required = false) List<String> imageUrls) throws IOException {

        List<FileInfo> imageInfo = new ArrayList<>();
        MultipartFile productImage = images.get(0);
        images.remove(0);
        String productFileName = productImage.getOriginalFilename();
        UploadResult productImgResult = fireBaseService.uploadFiles(productImage, PATH, productFileName);
        imageInfo.add(new FileInfo(productImgResult.getFirebaseImageUrl(), productImgResult.getFileName()));

        String description = form.getDescription();
        for (int i = 0; i < images.size(); i++) {
            MultipartFile file = images.get(i);
            String editorFileName = images.get(i).getOriginalFilename();
            UploadResult editorImgResult = fireBaseService.uploadFiles(file, PATH, editorFileName);

            // editorFirebaseImageUrl blobUrl을 비교해 같으면 해당 상세내용의 src을 editorFirebaseImageUrl로 변경
            String editorFirebaseImageUrl = editorImgResult.getFirebaseImageUrl();
            String blobUrl = imageUrls.get(i);
            description = productService.getUpdatedDescription(description, editorFirebaseImageUrl, blobUrl);

            imageInfo.add(new FileInfo(editorFirebaseImageUrl, editorImgResult.getFileName()));
        }

        Long categoryId = categoryService.selectIdByCategoryName(form.getSort());
        Product product = Product.builder()
                .categoryId(categoryId)
                .productName(form.getProductName())
                .productPrice(form.getPrice())
                .productStock(form.getStock())
                .productDescription(description)
                .productDiscount(form.getDiscount())
                .productSaleStatus(ON_SALE)
                .build();
        productService.insertProduct(product);

        for (int i = 0; i < imageInfo.size(); i++) {
            Image image = Image.builder()
                    .productId(product.getProductId())
                    .imageUrl(imageInfo.get(i).getFirebaseImageUrl())
                    .path(PATH)
                    .fileName(imageInfo.get(i).getFileName())
                    .build();
            imageService.insertImage(image);
        }

        return "success";
    }

    @GetMapping("/searchfilter")
    public PageResDto searchFilter(
            @RequestParam HashMap<String, String> params,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        String status = params.get("status");
        String category = params.get("category");
        String searchCond = params.get("searchCond");
        PageInfo<ProductResDto> pageInfo = productService.getProductsByFilter(pageNum, pageSize, status, category, searchCond);
        PageResDto pageResDto = new PageResDto(pageInfo, status, category);

        return pageResDto;
    }

    @PostMapping("/salestop")
    public String clickSaleStopBtn(@RequestBody SaleStopDto saleStopDTO) {
        productService.updateProductStatus(saleStopDTO.getProductId(), saleStopDTO.getStatus());
        Product updatedProduct = productService.selectProductAllById(saleStopDTO.getProductId());
        String updatedTime = String.valueOf(updatedProduct.getProductUpdatedAt());

        return updatedTime;
    }

    @PostMapping("/edit/product/{productId}")
    public String updateProduct(@PathVariable("productId") Long productId,
                                @ModelAttribute ProductRegistrationForm form,
                                @RequestParam(value = "productImage", required = false) MultipartFile productImage,
                                @RequestParam(value = "editorImages", required = false) List<MultipartFile> editorImages,
                                @RequestParam(value = "imageUrls", required = false) List<String> imageUrls) throws IOException {
        Product findProduct = productService.selectProductAllById(productId);
        List<Image> findImages = imageService.selectImagesByProductId(findProduct.getProductId());
        List<FileInfo> imageInfo = new ArrayList<>();

        // 상품이미지가 교체되었을 경우 기존에 저장되어 있는 이미지를 삭제하고 새로 받은 이미지를 업로드
        if (productImage != null) {
            fireBaseService.deleteStoredImage(PATH, findImages.get(0).getFileName());
            UploadResult productImgResult = fireBaseService.uploadFiles(productImage, PATH, productImage.getOriginalFilename());
            Long productImgId = findImages.get(0).getImageId();
            imageInfo.add(new FileInfo(productImgId, productImgResult.getFirebaseImageUrl(), productImgResult.getFileName()));
        }

        String description = form.getDescription();
        if (editorImages != null && !editorImages.isEmpty() && imageUrls != null && !imageUrls.isEmpty()) {
            for (int i = 0; i < editorImages.size(); i++) {
                MultipartFile file = editorImages.get(i);
                String editorFileName = editorImages.get(i).getOriginalFilename();
                UploadResult editorImgResult = fireBaseService.uploadFiles(file, PATH, editorFileName);

                // editorImgUrl과 blobUrl을 비교해 같으면 해당 상세내용의 src을 editorImgUrl로 변경
                String editorImgUrl = editorImgResult.getFirebaseImageUrl();
                String blobUrl = imageUrls.get(i);
                description = productService.getUpdatedDescription(description, editorImgUrl, blobUrl);

                // 기존 이미지와 폼에서 받은 에디터 이미지가 다르다면 기존 이미지를 firebase에서 삭제
                Image editorImg = findImages.get(i + 1);
                String currentImageUrl = editorImg.getImageUrl();
                if (!editorImgUrl.equals(currentImageUrl)) {
                    fireBaseService.deleteStoredImage(PATH, currentImageUrl);
                }

                imageInfo.add(new FileInfo(editorImg.getImageId(), editorImgUrl, editorFileName));
            }
        }

        Long categoryId = categoryService.selectIdByCategoryName(form.getSort());
        Product product = Product.builder()
                .productId(findProduct.getProductId())
                .categoryId(categoryId)
                .productName(form.getProductName())
                .productPrice(form.getPrice())
                .productStock(form.getStock())
                .productDescription(description)
                .productDiscount(form.getDiscount())
                .productSaleStatus(ON_SALE)
                .build();
        productService.updateProduct(product);

        for (FileInfo fileInfo : imageInfo) {
            Image image = Image.builder()
                    .imageId(fileInfo.getImageId())
                    .imageUrl(fileInfo.getFirebaseImageUrl())
                    .path(PATH)
                    .fileName(fileInfo.getFileName())
                    .build();

            imageService.updateImage(image);
        }

        return "success";
    }

    @DeleteMapping("/delete/{productId}")
    public String deleteProduct(@PathVariable Long productId) {
        List<String> fileNames = imageService.selectFileNamesByProductId(productId);
        for (String fileName : fileNames) {
            fireBaseService.deleteStoredImage(PATH, fileName);
        }
        imageService.deleteImagesByProductId(productId);
        productService.deleteProductById(productId);
        return "success";
    }
}
