package developmentpermission.entity.key;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Data;

@Embeddable
@Data
public class ClaimedCategoryKey implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Column(name = "application_id")
	private String applicationId;
	
	@Column(name = "view_id")
	private String viewId;
	
	@Column(name = "category_id")
	private String categoryId;
}
