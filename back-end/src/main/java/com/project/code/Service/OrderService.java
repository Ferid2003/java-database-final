package com.project.code.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.project.code.Model.Customer;
import com.project.code.Model.Inventory;
import com.project.code.Model.OrderDetails;
import com.project.code.Model.OrderItem;
import com.project.code.Model.PlaceOrderRequestDTO;
import com.project.code.Model.Product;
import com.project.code.Model.PurchaseProductDTO;
import com.project.code.Model.Store;
import com.project.code.Repo.CustomerRepository;
import com.project.code.Repo.InventoryRepository;
import com.project.code.Repo.OrderDetailsRepository;
import com.project.code.Repo.OrderItemRepository;
import com.project.code.Repo.ProductRepository;
import com.project.code.Repo.StoreRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private OrderDetailsRepository orderDetailsRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;
// 1. **saveOrder Method**:
//    - Processes a customer's order, including saving the order details and associated items.
//    - Parameters: `PlaceOrderRequestDTO placeOrderRequest` (Request data for placing an order)
//    - Return Type: `void` (This method doesn't return anything, it just processes the order)

// 2. **Retrieve or Create the Customer**:
//    - Check if the customer exists by their email using `findByEmail`.
//    - If the customer exists, use the existing customer; otherwise, create and save a new customer using `customerRepository.save()`.

// 3. **Retrieve the Store**:
//    - Fetch the store by ID from `storeRepository`.
//    - If the store doesn't exist, throw an exception. Use `storeRepository.findById()`.

// 4. **Create OrderDetails**:
//    - Create a new `OrderDetails` object and set customer, store, total price, and the current timestamp.
//    - Set the order date using `java.time.LocalDateTime.now()` and save the order with `orderDetailsRepository.save()`.

// 5. **Create and Save OrderItems**:
//    - For each product purchased, find the corresponding inventory, update stock levels, and save the changes using `inventoryRepository.save()`.
//    - Create and save `OrderItem` for each product and associate it with the `OrderDetails` using `orderItemRepository.save()`.

    public void saveOrder(PlaceOrderRequestDTO placeOrderRequestDTO){
        Customer customer = customerRepository.findByEmail(placeOrderRequestDTO.getCustomerEmail());

        if(customer == null){
            customer = customerRepository.save(new Customer());
        }

        Store store = storeRepository.findById(placeOrderRequestDTO.getStoreId()).orElseThrow(() -> new RuntimeException("Store with provided id is not present."));

        OrderDetails orderDetails = new OrderDetails(customer, store, placeOrderRequestDTO.getTotalPrice(), LocalDateTime.now());
        orderDetailsRepository.save(orderDetails);
        List<OrderItem> orderItems = new ArrayList<>();

        for(PurchaseProductDTO purchaseProductDTO : placeOrderRequestDTO.getPurchaseProduct()){
            Inventory inventory = inventoryRepository.findByProductIdandStoreId(purchaseProductDTO.getId(), placeOrderRequestDTO.getStoreId());
            inventory.setStockLevel(inventory.getStockLevel()-purchaseProductDTO.getQuantity());
            inventoryRepository.save(inventory);

            Product product = productRepository.findById(purchaseProductDTO.getId()).orElseThrow();
            OrderItem orderItem = new OrderItem(orderDetails, product, purchaseProductDTO.getQuantity(),purchaseProductDTO.getPrice());
            orderItems.add(orderItem);
            orderItemRepository.save(orderItem);
        }

        orderDetails.setOrderItems(orderItems);
        orderDetailsRepository.save(orderDetails);

    }
   
}
