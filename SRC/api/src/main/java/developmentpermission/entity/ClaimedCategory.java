package developmentpermission.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import developmentpermission.entity.key.ClaimedCategoryKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * O_申請済み区分Entityクラス.
 *
 *
 */
@Entity
@Data
@Table(name = "o_application_category")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@IdClass(value=ClaimedCategoryKey.class)
public class ClaimedCategory implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "application_id")
	private String applicationId;
	
	@Id
	@Column(name = "view_id")
	private String viewId;
	
	@Id
	@Column(name = "category_id")
	private String categoryId;
	
	@ManyToOne
    @JoinColumn(name = "view_id", referencedColumnName = "view_id")
    @MapsId("viewId")
    private ApplicationCategorySelectionView applicationCategorySelectionView;
	
	@ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "category_id")
    @MapsId("categoryId")
    private ApplicationCategoryMaster applicationCategory;
	
}
