package eu.daiad.web.repository.application;

import com.ibm.icu.text.MessageFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.text.NumberFormat;
import org.joda.time.DateTime;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import javax.persistence.PersistenceContext;

import eu.daiad.web.model.recommendation.Recommendation;
import eu.daiad.web.model.recommendation.RecommendationStatic;
import eu.daiad.web.domain.application.Account;
import eu.daiad.web.domain.application.AccountAlert;
import eu.daiad.web.domain.application.AccountAlertProperty;
import eu.daiad.web.domain.application.AccountDynamicRecommendation;
import eu.daiad.web.domain.application.AccountDynamicRecommendationProperty;
import eu.daiad.web.domain.application.AlertTranslation;
import eu.daiad.web.domain.application.DynamicRecommendationTranslation;
import eu.daiad.web.domain.application.StaticRecommendation;
import eu.daiad.web.model.recommendation.EnumAlerts;
import eu.daiad.web.model.recommendation.EnumDynamicRecommendations;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SharedErrorCode;
import org.joda.time.DateTimeConstants;

/**
 * Repository for retrieving messages for users and setting acknowledgements.
 * 
 * @author nkarag
 */

@Repository
@Transactional("transactionManager")
@Scope("prototype")
public class JpaMessageQueryRepository implements IMessageQueryRepository {

    @PersistenceContext(unitName="default")
    EntityManager entityManager;

    private final int staticTipInterval = DateTimeConstants.DAYS_PER_WEEK;;      
    private final String currencyKey1 = "currency1";
    private final String currencyKey2 = "currency2";
    
    @Override
    public void messageAcknowledged(String username, String type, int messageId, DateTime acknowledgedOn){

        switch (type){
            case "alert":              
                persistAlertAcknowledgement(username, messageId, acknowledgedOn);
                break;
            case "recommendation":
                persistRecommendationAcknowledgement(username, messageId, acknowledgedOn);
                break;
            case "static":
                break;
            default:
                throw new ApplicationException(SharedErrorCode.UNKNOWN).set("Unknown message acknowledgement type", type);
        }
    }    

    @Override
    public List<Recommendation> getMessages(String username){
        Account account = getAccountByUsername(username);
        int accountId = account.getId();

        String locale = decideLocale(account.getLocale());       
        Locale currencySymbol = decideCurrency(account.getCountry());
        
        List<Recommendation> messages = new ArrayList<>();
        TypedQuery<eu.daiad.web.domain.application.AccountAlert> accountAlertsQuery = entityManager
            .createQuery("select a from account_alert a where a.accountId = :accountId order by a.createdOn desc",
                    eu.daiad.web.domain.application.AccountAlert.class);
        accountAlertsQuery.setParameter("accountId", accountId);
       
        List<AccountAlert> accountAlerts = accountAlertsQuery.getResultList();
        HashSet<Integer> uniqueAlerts = new HashSet<>();
        
        for (AccountAlert accountAlert : accountAlerts) {
            if(accountAlert.getAcknowledgedOn() == null){

                TypedQuery<eu.daiad.web.domain.application.AlertTranslation> 
                    alertTranslationQuery = entityManager.createQuery
                        ("select a from alert_translation a where a.alertId = :alertId and a.locale = :locale",
                            eu.daiad.web.domain.application.AlertTranslation.class).setFirstResult(0)
                                .setMaxResults(1);
                alertTranslationQuery.setParameter("alertId", accountAlert.getAlertId());
                alertTranslationQuery.setParameter("locale", locale);

                AlertTranslation alertTranslated = alertTranslationQuery.getSingleResult();

                TypedQuery<eu.daiad.web.domain.application.AccountAlertProperty> 
                    accountAlertPropertyQuery = entityManager.createQuery
                        ("select a from account_alert_property a where a.accountAlertId = :accountAlertId",
                                eu.daiad.web.domain.application.AccountAlertProperty.class);                
                accountAlertPropertyQuery.setParameter("accountAlertId", accountAlert.getId());
                
                List<AccountAlertProperty> accountAlertPropertyList = accountAlertPropertyQuery.getResultList();            

                Map<String, String> formatProperties = new HashMap<>();
                
                for(AccountAlertProperty property : accountAlertPropertyList){                    
                    setFormatProperties(property, formatProperties, currencySymbol);                   
                }

                MessageFormat titleTemplate = new MessageFormat(alertTranslated.getTitle(), currencySymbol); 
                
                String alertTitleReady = titleTemplate.format(formatProperties);                                            
                
                MessageFormat descriptionTemplate;
                String alertDescriptionReady;
                
                Recommendation alert;
                if(alertTranslated.getDescription() != null){
                    descriptionTemplate = new MessageFormat(alertTranslated.getDescription(), new Locale(locale));
                    alertDescriptionReady = descriptionTemplate.format(formatProperties);
                    alert = createAlert(alertTranslated, alertTitleReady, alertDescriptionReady);
                }
                else{
                    alert = createAlert(alertTranslated, alertTitleReady);
                }

                if(!uniqueAlerts.contains(alertTranslated.getAlertId())){
                    messages.add(alert);
                    uniqueAlerts.add(alertTranslated.getAlertId());
                }
            }           
        }
        
        TypedQuery<eu.daiad.web.domain.application.AccountDynamicRecommendation> 
            accountRecommendationQuery = entityManager.createQuery
                ("select a from account_dynamic_recommendation a where a.accountId = :accountId order by a.createdOn desc",
                    eu.daiad.web.domain.application.AccountDynamicRecommendation.class);
        accountRecommendationQuery.setParameter("accountId", accountId);        
        
        List<AccountDynamicRecommendation> accountRecommendations = accountRecommendationQuery.getResultList();
        HashSet<Integer> uniqueRecommendations = new HashSet<>();

        for(AccountDynamicRecommendation accountRecommendation : accountRecommendations){
            
            if(accountRecommendation.getAcknowledgedOn() == null){  
               
                TypedQuery<eu.daiad.web.domain.application.DynamicRecommendationTranslation> 
                    recommendationTranslationQuery = entityManager.createQuery
                        ("select a from dynamic_recommendation_translation a where "
                            + "a.dynamicRecommendationId = :dynamicRecommendationId and a.locale = :locale",
                            eu.daiad.web.domain.application.DynamicRecommendationTranslation.class)
                                .setFirstResult(0)
                                    .setMaxResults(1);

                recommendationTranslationQuery
                        .setParameter("dynamicRecommendationId", accountRecommendation.getDynamicRecommendationId());
                recommendationTranslationQuery.setParameter("locale", locale);
             
                DynamicRecommendationTranslation recommendationTranslated 
                        = recommendationTranslationQuery.getSingleResult();

                
                TypedQuery<eu.daiad.web.domain.application.AccountDynamicRecommendationProperty> 
                    accountRecommendationPropertyQuery = entityManager.createQuery
                        ("select a from account_dynamic_recommendation_property a "
                                + "where a.accountDynamicRecommendationId = :accountDynamicRecommendationId",
                                eu.daiad.web.domain.application.AccountDynamicRecommendationProperty.class);                
                accountRecommendationPropertyQuery
                        .setParameter("accountDynamicRecommendationId", accountRecommendation.getId());
                
                 List<AccountDynamicRecommendationProperty> accountRecommendationPropertyList;
                 
                accountRecommendationPropertyList = accountRecommendationPropertyQuery.getResultList();                    

                Map<String, String> formatProperties = new HashMap<>();
                for(AccountDynamicRecommendationProperty property : accountRecommendationPropertyList){
                    setFormatProperties(property, formatProperties, currencySymbol);
                }

                MessageFormat titleTemplate = new MessageFormat(recommendationTranslated.getTitle());     
                MessageFormat descriptionTemplate = new MessageFormat(recommendationTranslated.getDescription());
                
                String recommendationTitleReady = titleTemplate.format(formatProperties);
                String recommendationDescriptionReady = descriptionTemplate.format(formatProperties);

                Recommendation recommendation = createRecommendation
                                (recommendationTranslated, recommendationTitleReady, recommendationDescriptionReady);
                                                
                if(!uniqueRecommendations.contains(recommendationTranslated.getDynamicRecommendationId())){
                    messages.add(recommendation);
                    uniqueRecommendations.add(recommendationTranslated.getDynamicRecommendationId());
                }                
            }          
        }

        //add a random static tip every week.
        DateTime lastSent = account.getProfile().getStaticTipSentOn();
                
        if(lastSent.isBefore(DateTime.now().minusDays(staticTipInterval))){
            Recommendation staticRecommendation = getRandomStaticAlert(account.getLocale());
            messages.add(staticRecommendation);            
        }
        return messages;
    }     

    
    private Recommendation getRandomStaticAlert(String accountLocale){
        String locale;
        
        switch (accountLocale) {
            case "en":
                locale = accountLocale;
                break;
            case "es":
                locale = accountLocale;
                break;
            default:
                locale = "en";                                
        }
        
        TypedQuery<eu.daiad.web.domain.application.StaticRecommendation> accountAlertsQuery = entityManager
                .createQuery("select a from static_recommendation a where a.locale = :locale",
                        eu.daiad.web.domain.application.StaticRecommendation.class);    
        accountAlertsQuery.setParameter("locale", locale);
        
        List<StaticRecommendation> res = accountAlertsQuery.getResultList();   
        
        Random random = new Random();
        int max = res.size();
        int min = res.get(0).getIndex();
        int range = max - min;        
        int randomId = random.nextInt(range) + min;
        
        StaticRecommendation singleRandomStaticRecommendation = res.get(randomId);
        
        RecommendationStatic staticRec = new RecommendationStatic(); //RecommendationStatic or Recommendation
        staticRec.setId(singleRandomStaticRecommendation.getIndex());       
        staticRec.setType("static");
        staticRec.setTitle(singleRandomStaticRecommendation.getTitle());
        staticRec.setDescription(singleRandomStaticRecommendation.getDescription());
        staticRec.setCategory(singleRandomStaticRecommendation.getCategory().getId());
        staticRec.setSource(singleRandomStaticRecommendation.getSource());
        staticRec.setImage(singleRandomStaticRecommendation.getImage());
        staticRec.setImageLink(singleRandomStaticRecommendation.getImageLink());
        staticRec.setExternaLink(singleRandomStaticRecommendation.getExternaLink());
        staticRec.setPrompt(singleRandomStaticRecommendation.getPrompt());
        
        return staticRec;
    }  
    
    private void persistAlertAcknowledgement(String username, int alertId, DateTime acknowledgedOn){
        
        Account account = getAccountByUsername(username);
        int accountId = account.getId();        
        
        TypedQuery<eu.daiad.web.domain.application.AccountAlert> accountAlertsQuery = entityManager
                .createQuery("select a from account_alert a where a.accountId = :accountId and a.alertId = :alertId",
                        eu.daiad.web.domain.application.AccountAlert.class);
        accountAlertsQuery.setParameter("accountId", accountId);   
        accountAlertsQuery.setParameter("alertId", alertId);
        
        List<AccountAlert> res = accountAlertsQuery.getResultList();
        
        for(AccountAlert accountAlert : res){
            if(accountAlert.getAcknowledgedOn() == null){
                System.out.println("ack on: " + acknowledgedOn);
                accountAlert.setAcknowledgedOn(acknowledgedOn);
                entityManager.persist(accountAlert);
                entityManager.flush(); 
            }            
        }      
    }
    
    private void persistRecommendationAcknowledgement(String username, int recommendationId, DateTime acknowledgedOn){
        
        Account account = getAccountByUsername(username);
        int accountId = account.getId();        
        
        TypedQuery<eu.daiad.web.domain.application.AccountDynamicRecommendation> accountDynamicRecommendationQuery 
                = entityManager.createQuery("select a from account_dynamic_recommendation a "
                    + "where a.accountId = :accountId and a.dynamicRecommendationId = :dynamicRecommendationId",
                        eu.daiad.web.domain.application.AccountDynamicRecommendation.class);
        
        accountDynamicRecommendationQuery.setParameter("accountId", accountId);   
        accountDynamicRecommendationQuery.setParameter("dynamicRecommendationId", recommendationId);
        
        List<AccountDynamicRecommendation> recommendations = accountDynamicRecommendationQuery.getResultList();
        
        for(AccountDynamicRecommendation accountDynamicRecommendation : recommendations){
            if(accountDynamicRecommendation.getAcknowledgedOn() == null){
                System.out.println("ack on: " + acknowledgedOn);
                accountDynamicRecommendation.setAcknowledgedOn(acknowledgedOn);
                entityManager.persist(accountDynamicRecommendation);
                entityManager.flush(); 
            }            
        }      
    }
    
    private void resetAcknowledgements(String username, int alertId){
        Account account = getAccountByUsername(username);
        int accountId = account.getId();        
        
        TypedQuery<eu.daiad.web.domain.application.AccountAlert> accountAlertsQuery = entityManager.createQuery
            ("select a from account_alert a where a.accountId = :accountId and a.alertId = :alertId",
                eu.daiad.web.domain.application.AccountAlert.class);
        accountAlertsQuery.setParameter("accountId", accountId);   
        accountAlertsQuery.setParameter("alertId", alertId);
        
        List<AccountAlert> accountAlerts = accountAlertsQuery.getResultList();
        
        for(AccountAlert accountAlert : accountAlerts){
            if(accountAlert.getAcknowledgedOn() != null){
                accountAlert.setAcknowledgedOn(null);
                entityManager.persist(accountAlert);
                entityManager.flush(); 
            }            
        }      
    }

    private Account getAccountByUsername(String username) {

        TypedQuery<eu.daiad.web.domain.application.Account> query = entityManager
                .createQuery("select a from account a where a.username = :username",
                        eu.daiad.web.domain.application.Account.class).setFirstResult(0)
                .setMaxResults(1);
        query.setParameter("username", username);
        Account account = query.getSingleResult();
        
        return account;
    }
      
    private Locale decideCurrency(String country){
        Locale currency;
        switch (country){            
            case "United Kingdom": //TODO - check fixed values of countries
                //currencyRate = "GBP";
                currency = Locale.UK;
                break; 
            default:
                //currencyRate = "EUR";
                currency = Locale.GERMANY;
        }       
        return currency;
    }
    
    private String decideLocale(String accountLocale){
        String locale;
        switch (accountLocale){            
            case "en":
                locale = accountLocale;
                break; 
            case "es":
                locale = accountLocale;
                break;
            default:
                locale = "en";
        }
        return locale;
    }
    
    private void setFormatProperties(AccountAlertProperty property, 
                                    Map<String, String> formatProperties, Locale currencySymbol){        
        switch (property.getKey()) {
            case currencyKey1:
                {
                    NumberFormat numberFormat = NumberFormat.getCurrencyInstance(currencySymbol);
                    numberFormat.setMaximumFractionDigits(1);
                    float euros = Float.parseFloat(property.getValue());
                    float money = convertCurrencyIfNeed(euros, currencySymbol);

                    String currencyFormatted = numberFormat.format(money);
                    formatProperties.put(property.getKey(), currencyFormatted);
                    break;
                }
            case currencyKey2:
                {
                    NumberFormat numberFormat = NumberFormat.getCurrencyInstance(currencySymbol);
                    numberFormat.setMaximumFractionDigits(1);
                    float euros = Float.parseFloat(property.getValue());
                    float money = convertCurrencyIfNeed(euros, currencySymbol);

                    String currencyFormatted = numberFormat.format(money);
                    formatProperties.put(property.getKey(), currencyFormatted);
                    break;
                }
            default:
                formatProperties.put(property.getKey(), property.getValue());
                break;
        }    
    }
    
    private void setFormatProperties(AccountDynamicRecommendationProperty property, 
                                    Map<String, String> formatProperties, Locale currencySymbol){         
        switch (property.getKey()) {
            case currencyKey1:
                {
                    NumberFormat numberFormat = NumberFormat.getCurrencyInstance(currencySymbol);
                    numberFormat.setMaximumFractionDigits(1);
                    float euros = Float.parseFloat(property.getValue());
                    float money = convertCurrencyIfNeed(euros, currencySymbol);

                    String currencyFormatted = numberFormat.format(money);
                    formatProperties.put(property.getKey(), currencyFormatted);
                    break;
                }
            case currencyKey2:
                {
                    NumberFormat numberFormat = NumberFormat.getCurrencyInstance(currencySymbol);
                    numberFormat.setMaximumFractionDigits(1);
                    float euros = Float.parseFloat(property.getValue());
                    float money = convertCurrencyIfNeed(euros, currencySymbol);

                    String currencyFormatted = numberFormat.format(money);
                    formatProperties.put(property.getKey(), currencyFormatted);
                    break;
                }
            default:
                formatProperties.put(property.getKey(), property.getValue());
                break;
        }    
    }    

    private Recommendation createRecommendation(DynamicRecommendationTranslation recommendationTranslated,
            String titleFormatted, String descriptionFormatted){
        
        Recommendation recommendation = new Recommendation();
        recommendation.setId(recommendationTranslated.getDynamicRecommendationId());
        recommendation.setType(EnumDynamicRecommendations.getType());
        recommendation.setTitle(titleFormatted);
        recommendation.setDescription(descriptionFormatted);
        recommendation.setImageLink(recommendationTranslated.getImageLink());

        return recommendation;
    }       

    private Recommendation createAlert(AlertTranslation alertTranslated, String titleFormatted, String descriptionFormatted){

        Recommendation alert = new Recommendation();
        alert.setId(alertTranslated.getAlertId());
        alert.setType(EnumAlerts.getType());
        alert.setTitle(titleFormatted);
        alert.setDescription(descriptionFormatted);
        alert.setImageLink(alertTranslated.getImageLink());

        return alert;
    } 
    
    private Recommendation createAlert(AlertTranslation alertTranslated, String titleFormatted){

        Recommendation alert = new Recommendation();
        alert.setId(alertTranslated.getAlertId());
        alert.setType(EnumAlerts.getType());
        alert.setTitle(titleFormatted);
        alert.setImageLink(alertTranslated.getImageLink());
        
        return alert;
    }
    
    private float convertCurrencyIfNeed(float euros, Locale currencySymbol){
        //this is dummy method for future use. Currently returns only euros. 
        //The currency is converted in the message computation for now and only for KWH prices         
        if(currencySymbol.equals(Locale.GERMANY)){
            return euros;
        }
        else if(currencySymbol.equals(Locale.UK)){
            return euros;
            //return (float) (euros*0.8); //get currency rate from db
        }
        else{
            return euros;
        }
    }   

    @Override
    public List<Recommendation> testGetMessages(){

        int accountId = 6666;
        Locale currencySymbol = Locale.GERMANY;      
        String locale = "en";

        List<Recommendation> messages = new ArrayList<>();
        TypedQuery<eu.daiad.web.domain.application.AccountAlert> accountAlertsQuery = entityManager
            .createQuery("select a from account_alert a where a.accountId = :accountId order by a.createdOn desc",
                    eu.daiad.web.domain.application.AccountAlert.class);
        accountAlertsQuery.setParameter("accountId", accountId);
       
        List<AccountAlert> accountAlerts = accountAlertsQuery.getResultList();
        HashSet<Integer> uniqueAlerts = new HashSet<>();
        
        for (AccountAlert accountAlert : accountAlerts) {
            if(accountAlert.getAcknowledgedOn() == null){

                TypedQuery<eu.daiad.web.domain.application.AlertTranslation> 
                    alertTranslationQuery = entityManager.createQuery
                        ("select a from alert_translation a where a.alertId = :alertId and a.locale = :locale",
                            eu.daiad.web.domain.application.AlertTranslation.class).setFirstResult(0)
                                .setMaxResults(1);
                alertTranslationQuery.setParameter("alertId", accountAlert.getAlertId());
                alertTranslationQuery.setParameter("locale", locale);

                AlertTranslation alertTranslated = alertTranslationQuery.getSingleResult();

                TypedQuery<eu.daiad.web.domain.application.AccountAlertProperty> 
                    accountAlertPropertyQuery = entityManager.createQuery
                        ("select a from account_alert_property a where a.accountAlertId = :accountAlertId",
                                eu.daiad.web.domain.application.AccountAlertProperty.class);                
                accountAlertPropertyQuery.setParameter("accountAlertId", accountAlert.getId());
                
                List<AccountAlertProperty> accountAlertPropertyList = accountAlertPropertyQuery.getResultList();            

                Map<String, String> formatProperties = new HashMap<>();
                for(AccountAlertProperty property : accountAlertPropertyList){
                    setFormatProperties(property, formatProperties, currencySymbol);                   
                }

                MessageFormat titleTemplate = new MessageFormat(alertTranslated.getTitle(), currencySymbol); 
                
                String alertTitleReady = titleTemplate.format(formatProperties);                                            
                
                MessageFormat descriptionTemplate;
                String alertDescriptionReady;
                
                Recommendation alert;
                if(alertTranslated.getDescription() != null){
                    descriptionTemplate = new MessageFormat(alertTranslated.getDescription(), new Locale(locale));
                    alertDescriptionReady = descriptionTemplate.format(formatProperties);
                    alert = createAlert(alertTranslated, alertTitleReady, alertDescriptionReady);
                }
                else{
                    alert = createAlert(alertTranslated, alertTitleReady);
                }

                if(!uniqueAlerts.contains(alertTranslated.getAlertId())){
                    messages.add(alert);
                    uniqueAlerts.add(alertTranslated.getAlertId());
                }
            }           
        }
        
        TypedQuery<eu.daiad.web.domain.application.AccountDynamicRecommendation> 
            accountRecommendationQuery = entityManager.createQuery
                ("select a from account_dynamic_recommendation a where a.accountId = :accountId order by a.createdOn desc",
                    eu.daiad.web.domain.application.AccountDynamicRecommendation.class);
        accountRecommendationQuery.setParameter("accountId", accountId);        
        
        List<AccountDynamicRecommendation> accountRecommendations = accountRecommendationQuery.getResultList();
        HashSet<Integer> uniqueRecommendations = new HashSet<>();

        for(AccountDynamicRecommendation accountRecommendation : accountRecommendations){
            
            if(accountRecommendation.getAcknowledgedOn() == null){  

                TypedQuery<eu.daiad.web.domain.application.DynamicRecommendationTranslation> 
                    recommendationTranslationQuery = entityManager.createQuery
                        ("select a from dynamic_recommendation_translation a where "
                            + "a.dynamicRecommendationId = :dynamicRecommendationId and a.locale = :locale",
                            eu.daiad.web.domain.application.DynamicRecommendationTranslation.class)
                                .setFirstResult(0)
                                    .setMaxResults(1);

                recommendationTranslationQuery
                        .setParameter("dynamicRecommendationId", accountRecommendation.getDynamicRecommendationId());
                recommendationTranslationQuery.setParameter("locale", locale);
             
                DynamicRecommendationTranslation recommendationTranslated 
                        = recommendationTranslationQuery.getSingleResult();

                TypedQuery<eu.daiad.web.domain.application.AccountDynamicRecommendationProperty> 
                    accountRecommendationPropertyQuery = entityManager.createQuery
                        ("select a from account_dynamic_recommendation_property a "
                                + "where a.accountDynamicRecommendationId = :accountDynamicRecommendationId",
                                eu.daiad.web.domain.application.AccountDynamicRecommendationProperty.class);                
                accountRecommendationPropertyQuery
                        .setParameter("accountDynamicRecommendationId", accountRecommendation.getId());
                
                List<AccountDynamicRecommendationProperty> accountRecommendationPropertyList;
                 
                accountRecommendationPropertyList = accountRecommendationPropertyQuery.getResultList();                    

                Map<String, String> formatProperties = new HashMap<>();
                for(AccountDynamicRecommendationProperty property : accountRecommendationPropertyList){
                    setFormatProperties(property, formatProperties, currencySymbol);
                }

                MessageFormat titleTemplate = new MessageFormat(recommendationTranslated.getTitle());     
                MessageFormat descriptionTemplate = new MessageFormat(recommendationTranslated.getDescription());
                
                String recommendationTitleReady = titleTemplate.format(formatProperties);
                String recommendationDescriptionReady = descriptionTemplate.format(formatProperties);

                Recommendation recommendation = createRecommendation
                                (recommendationTranslated, recommendationTitleReady, recommendationDescriptionReady);
                                                
                if(!uniqueRecommendations.contains(recommendationTranslated.getDynamicRecommendationId())){
                    messages.add(recommendation);
                    uniqueRecommendations.add(recommendationTranslated.getDynamicRecommendationId());
                }                
            }          
        }

        //returns a single static tip always
        Recommendation staticRecommendation = getRandomStaticAlert(locale);
        messages.add(staticRecommendation);            

        return messages;
    } 
    
//    private float convertEURtoGBP(float euros){
//        TypedQuery<eu.daiad.web.domain.Currency> query = entityManager
//                .createQuery("select a from currency a where a.ISO_code = :ISO_code",
//                        eu.daiad.web.domain.Currency.class).setFirstResult(0)
//                .setMaxResults(1);
//        query.setParameter("ISO_code", "EUR");
//        Currency c = query.getSingleResult();
//        float p = c.getToGbp();
//        
//        return 4;        
//    }  
    
}
