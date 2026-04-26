package com.esprit.campconnect.Livraison.demo.controller;

import com.esprit.campconnect.Livraison.demo.dto.DemoCheckoutRequest;
import com.esprit.campconnect.Livraison.demo.dto.DemoCheckoutResponse;
import com.esprit.campconnect.Livraison.demo.dto.DemoPaymentRequest;
import com.esprit.campconnect.Livraison.demo.dto.DemoPaymentResponse;
import com.esprit.campconnect.Livraison.demo.entity.DemoProduit;
import com.esprit.campconnect.Livraison.demo.entity.DemoRepas;
import com.esprit.campconnect.Livraison.demo.service.DemoDataService;
import com.esprit.campconnect.Livraison.demo.service.DemoPaymentService;
import com.esprit.campconnect.Livraison.dto.LivraisonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/livraisons/demo")
@RequiredArgsConstructor
public class DemoController {

    private final DemoPaymentService paymentService;
    private final DemoDataService demoService;

    @GetMapping("/products")
    public List<DemoProduit> getProducts() {
        return demoService.getProduits();
    }

    @GetMapping("/repas")
    public List<DemoRepas> getRepas() {
        return demoService.getRepas();
    }

    @PostMapping("/checkout/classique")
    public DemoCheckoutResponse checkoutClassic(@RequestBody DemoCheckoutRequest request) {
        return demoService.createClassicCheckout(request);
    }

    @PostMapping("/checkout/repas")
    public DemoCheckoutResponse checkoutRepas(@RequestBody DemoCheckoutRequest request) {
        return demoService.createRepasCheckout(request);
    }

    @PostMapping("/payment/create-session")
    public DemoPaymentResponse createPaymentSession(@RequestBody DemoPaymentRequest request) {
        return paymentService.createCheckoutSession(request);
    }

    @GetMapping("/payment/success")
    public LivraisonResponse paymentSuccess(@RequestParam("session_id") String sessionId) {
        return paymentService.handlePaymentSuccess(sessionId);
    }
}