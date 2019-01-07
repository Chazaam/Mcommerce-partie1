package com.ecommerce.microcommerce.web.controller;

import com.ecommerce.microcommerce.dao.ProductDao;
import com.ecommerce.microcommerce.model.Product;
import com.ecommerce.microcommerce.web.exceptions.ProduitGratuitException;
import com.ecommerce.microcommerce.web.exceptions.ProduitIntrouvableException;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Api( description="API pour des opérations CRUD sur les produits.")

@RestController
public class ProductController {

    @Autowired
    private ProductDao productDao;
    
    //Récupérer la liste des produits
    @RequestMapping(value = "/Produits", method = RequestMethod.GET)
    public MappingJacksonValue listeProduits(@RequestParam(value = "triAlpha", defaultValue = "false") boolean triAlpha) {
        Iterable<Product> produits;
        if(triAlpha==true) {
        	produits = productDao.findByOrderByNomAsc();
	    }else {
	    	produits = productDao.findAll();
	    }        
        SimpleBeanPropertyFilter monFiltre = SimpleBeanPropertyFilter.serializeAllExcept("prixAchat");
        FilterProvider listDeNosFiltres = new SimpleFilterProvider().addFilter("monFiltreDynamique", monFiltre);
        MappingJacksonValue produitsFiltres = new MappingJacksonValue(produits);
        produitsFiltres.setFilters(listDeNosFiltres);
        
        return produitsFiltres;
    }

    //Récupérer un produit par son Id
    @ApiOperation(value = "Récupère un produit grâce à son ID à condition que celui-ci soit en stock!")
    @GetMapping(value = "/Produits/{id}")
    public Product afficherUnProduit(@PathVariable int id) {
        Product produit = productDao.findById(id);
        if(produit==null) throw new ProduitIntrouvableException("Le produit avec l'id " + id + " est INTROUVABLE. Écran Bleu si je pouvais.");
        return produit;
    }

    //ajouter un produit
    //@Chazaam PARTIE 3 ajouter une condition afin de vérifier que le prix de vente n’est pas égal à 0
    @PostMapping(value = "/Produits")
    public ResponseEntity<Void> ajouterProduit(@Valid @RequestBody Product product) {
        Product productAdded =  productDao.save(product);
        if (productAdded == null)
            return ResponseEntity.noContent().build();
        if (product.getPrix() <= 0) throw new  ProduitGratuitException("Le produit n'a pas pu être ajouté car son prix est incorrect: inférieur ou égale à 0.");
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(productAdded.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @DeleteMapping (value = "/Produits/{id}")
    public void supprimerProduit(@PathVariable int id) {
        productDao.delete(id);
    }

    @PutMapping (value = "/Produits")
    public void updateProduit(@RequestBody Product product) {
        productDao.save(product);
    }


    //Pour les tests
    @GetMapping(value = "test/produits/{prix}")
    public List<Product>  testeDeRequetes(@PathVariable int prix) {
        return productDao.chercherUnProduitCher(400);
    }

    //@Chazaam PARTIE 1 : calcule de la marge de chaque produit
    @GetMapping(value = "/AdminProduits")
   	public Map<String, Integer> calculerMargeProduit() {
    	List<Product> products = productDao.findAll();
    	
    	Map<String, Integer> map = new HashMap<String, Integer>();
    	for (Product product : products) {
    		map.put(product.toString(), product.getPrix()-product.getPrixAchat());    		
		}
    	return map;
   	}
    
    //@Chazaam PARTIE 2 : liste de tous les produits triés par nom croissant
    @GetMapping(value = "/trierProduitsParOrdreAlphabetique")
   	public List<Product> trierProduitsParOrdreAlphabetique(){
   		return productDao.findByOrderByNomAsc();
   	}

    
    
}
