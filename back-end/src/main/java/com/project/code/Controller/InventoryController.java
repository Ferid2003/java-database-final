package com.project.code.Controller;

import java.util.Map;

import com.project.code.Model.CombinedRequest;
import com.project.code.Model.Inventory;
import com.project.code.Repo.InventoryRepository;
import com.project.code.Repo.ProductRepository;
import com.project.code.Service.ServiceClass;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory")
public class InventoryController {
// 1. Set Up the Controller Class:
//    - Annotate the class with `@RestController` to indicate that this is a REST controller, which handles HTTP requests and responses.
//    - Use `@RequestMapping("/inventory")` to set the base URL path for all methods in this controller. All endpoints related to inventory will be prefixed with `/inventory`.
    

// 2. Autowired Dependencies:
//    - Autowire necessary repositories and services:
//      - `ProductRepository` will be used to interact with product data (i.e., finding, updating products).
//      - `InventoryRepository` will handle CRUD operations related to the inventory.
//      - `ServiceClass` will help with the validation logic (e.g., validating product IDs and inventory data).
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ServiceClass serviceClass;
// 3. Define the `updateInventory` Method:
//    - This method handles HTTP PUT requests to update inventory for a product.
//    - It takes a `CombinedRequest` (containing `Product` and `Inventory`) in the request body.
//    - The product ID is validated, and if valid, the inventory is updated in the database.
//    - If the inventory exists, update it and return a success message. If not, return a message indicating no data available.
    @PutMapping
    public Map<String, String> updateInventory(@RequestBody CombinedRequest combinedRequest){
        try {
            if(!serviceClass.ValidateProductId(combinedRequest.getProduct().getId())){
                return Map.of("message", "Product validation failed");
            }
            if(serviceClass.validateInventory(combinedRequest.getInventory())){
                inventoryRepository.save(combinedRequest.getInventory());
                productRepository.save(combinedRequest.getProduct());
                return Map.of("message", "Successfully updated product");
            }else{
                return Map.of("message", "No data abailable");
            }
        } catch (DataIntegrityViolationException e) {
            System.out.println("Exception occured while updating inventory:" + e.getMessage());
            return Map.of("message", "Product validation failed");
        }
    }

// 4. Define the `saveInventory` Method:
//    - This method handles HTTP POST requests to save a new inventory entry.
//    - It accepts an `Inventory` object in the request body.
//    - It first validates whether the inventory already exists. If it exists, it returns a message stating so. If it doesn’t exist, it saves the inventory and returns a success message.
    @PostMapping
    public Map<String, String> saveInventory(Inventory inventory){
        if(serviceClass.validateInventory(inventory)){
            return Map.of("message", "Data is already exists");
        }
        try {
            inventoryRepository.save(inventory);
            return Map.of("message", "Data saved successfully.");
        } catch (Exception e) {
            System.out.println("Exception occured while saving inventory");
            return Map.of("message", "Inventory saving failed");
        }
    }

// 5. Define the `getAllProducts` Method:
//    - This method handles HTTP GET requests to retrieve products for a specific store.
//    - It uses the `storeId` as a path variable and fetches the list of products from the database for the given store.
//    - The products are returned in a `Map` with the key `"products"`.
    @GetMapping("/{storeid}")
    public Map<String, Object> getAllProducts(@PathVariable long storeid){
        return Map.of("products", productRepository.findProductsByStoreId(storeid));
    }

// 6. Define the `getProductName` Method:
//    - This method handles HTTP GET requests to filter products by category and name.
//    - If either the category or name is `"null"`, adjust the filtering logic accordingly.
//    - Return the filtered products in the response with the key `"product"`.
    @GetMapping("filter/{category}/{name}/{storeid}")
    public Map<String, Object> getProductName(@PathVariable String category, @PathVariable String name, @PathVariable long storeid){
        if(category == null){
            return Map.of("products", productRepository.findByNameLike(storeid, name));
        }else if(name == null){
            return Map.of("products", productRepository.findProductByCategory(category, storeid));
        }else {
            return Map.of("products", productRepository.findByNameAndCategory(storeid, name, category));
        }
    }

// 7. Define the `searchProduct` Method:
//    - This method handles HTTP GET requests to search for products by name within a specific store.
//    - It uses `name` and `storeId` as parameters and searches for products that match the `name` in the specified store.
//    - The search results are returned in the response with the key `"product"`.
    @GetMapping("/search/{name}/{storeId}")
    public Map<String, Object> searchProduct(@PathVariable String name, @PathVariable long storeid){
        return Map.of("product", productRepository.findByNameLike(storeid, name));
    }

// 8. Define the `removeProduct` Method:
//    - This method handles HTTP DELETE requests to delete a product by its ID.
//    - It first validates if the product exists. If it does, it deletes the product from the `ProductRepository` and also removes the related inventory entry from the `InventoryRepository`.
//    - Returns a success message with the key `"message"` indicating successful deletion.
    @DeleteMapping("/{id}")
    public Map<String, String> removeProduct(@PathVariable long id){
        if(!serviceClass.ValidateProductId(id)){
            return Map.of("message", "Product does not exist in db");
        }
        inventoryRepository.deleteByProductId(id);
        return Map.of("message", "Product was deleted");
    }

// 9. Define the `validateQuantity` Method:
//    - This method handles HTTP GET requests to validate if a specified quantity of a product is available in stock for a given store.
//    - It checks the inventory for the product in the specified store and compares it to the requested quantity.
//    - If sufficient stock is available, return `true`; otherwise, return `false`.
    @GetMapping("validate/{quantity}/{storeId}/{productId}")
    public boolean validateQuantity(@PathVariable int quantity, @PathVariable long storeId, @PathVariable long productId){
        return inventoryRepository.findByProductIdandStoreId(productId, storeId).getStockLevel() >= quantity; 
    }
}
