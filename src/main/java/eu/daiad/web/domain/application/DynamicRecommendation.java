
package eu.daiad.web.domain.application;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author nkarag
 */
@Entity(name = "dynamic_recommendation")
@Table(schema = "public", name = "dynamic_recommendation")
//@Check(constraints = "mode::text = 'SWM'::text OR mode::text = 'AMPHIRO'::text OR mode::text = 'BOTH'::text")
//@Constraint(validatedBy = ModeValidator.class)
public class DynamicRecommendation {
    
    @Id()
    @Column(name = "id")
    private int id;   

    @Column(name = "mode")
    private String mode;        

    @Column(name = "priority")
    private int priority;  
        
    public int getId() {
            return id;
    }

    public void setId(int id) {
            this.id = id;
    }         
        
}
