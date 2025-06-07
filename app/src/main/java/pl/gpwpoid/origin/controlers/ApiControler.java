package pl.gpwpoid.origin.controlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.gpwpoid.origin.repositories.DTO.ActiveOrderDTO;
import pl.gpwpoid.origin.repositories.DTO.WalletCompanyDTO;
import pl.gpwpoid.origin.repositories.views.TransactionDTO;
import pl.gpwpoid.origin.services.OrderService;
import pl.gpwpoid.origin.services.TransactionService;
import pl.gpwpoid.origin.services.WalletsService;
import pl.gpwpoid.origin.ui.views.DTO.OrderDTO;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api")
@Validated
public class ApiControler {
    private final OrderService orderService;
    private final WalletsService walletsService;
    private final TransactionService transactionService;

    @Autowired
    public ApiControler(OrderService orderService, WalletsService walletsService, TransactionService transactionService) {
        this.orderService = orderService;
        this.walletsService = walletsService;
        this.transactionService = transactionService;
    }

    @PostMapping("/order")
    public ResponseEntity<?> addOrder(@RequestBody OrderDTO orderDTO){
        try{
            orderService.addOrder(orderDTO);
            return ResponseEntity.ok().build();
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/order/{orderId}")
    public ResponseEntity<?> cancelOrder(@PathVariable Integer orderId){
        try {
            orderService.cancelOrder(orderId);
            return ResponseEntity.ok().build();
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

        @DeleteMapping("/wallets/{walletId}/{companyId}/active_orders")
        public ResponseEntity<?> cancelWalletCompanyActiveOrders(@PathVariable Integer walletId, @PathVariable Integer companyId){
            try{
                List<ActiveOrderDTO> activeOrderDTOList = orderService.getActiveOrderDTOListByWalletIdCompanyId(walletId, companyId);
                for(ActiveOrderDTO activeOrderDTO : activeOrderDTOList){
                    orderService.cancelOrder(activeOrderDTO.getOrderId());
                }
                return ResponseEntity.ok().build();
            }
            catch (Exception e){
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }

    @GetMapping("/wallets/{walletId}/{companyId}")
    public ResponseEntity<?> getWalletCompanyInfo(@PathVariable Integer walletId, @PathVariable Integer companyId){
        try{
            WalletCompanyDTO walletCompanyDTO = walletsService.getWalletCompanyDTOByWalletIdCompanyId(walletId, companyId);
            return ResponseEntity.ok(walletCompanyDTO);
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }



    @GetMapping("/wallets/{walletId}/{companyId}/active_orders")
    public ResponseEntity<?> getWalletCompanyActiveOrders(@PathVariable Integer walletId, @PathVariable Integer companyId){
        try{
            List<ActiveOrderDTO> activeOrderDTOList = orderService.getActiveOrderDTOListByWalletIdCompanyId(walletId, companyId);
            return ResponseEntity.ok(activeOrderDTOList);
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/companies/{companyId}/transactions")
    public ResponseEntity<?> getCompanyTransactions(
            @PathVariable Integer companyId,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        try {
            List<TransactionDTO> transactions = transactionService.getCompanyTransactionDTOListByCompanyId(companyId, limit);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
