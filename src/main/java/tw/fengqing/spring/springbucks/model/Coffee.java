package tw.fengqing.spring.springbucks.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.joda.money.Money;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Convert;
import tw.fengqing.spring.springbucks.converter.MoneyConverter;
import java.io.Serializable;

@Entity
@Table(name = "T_COFFEE")
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Coffee extends BaseEntity implements Serializable {
    private String name;
    @Convert(converter = MoneyConverter.class)
    private Money price;
}
