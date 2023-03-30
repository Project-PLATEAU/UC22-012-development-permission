package developmentpermission.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 
 * O_申請地番Entityクラス.
 *
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ApplicationLotNumber implements Serializable{

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="seq_id")
	private Integer seqId;
	
	@Column(name="application_id")
	private Integer applicationId;
	
	@Column(name="lot_number_id")
	private Integer lotNumberId;
	
	@Column(name="status")
	private String status;
}
