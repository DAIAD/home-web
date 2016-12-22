package eu.daiad.web.domain.application;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import eu.daiad.web.model.favourite.EnumFavouriteType;

@Entity(name = "favourite_account")
@Table(schema = "public", name = "favourite_account")
public class FavouriteAccountEntity extends FavouriteEntity {

	@ManyToOne()
	@JoinColumn(name = "account_id", nullable = false)
	private AccountEntity account;

	public AccountEntity getAccount() {
		return account;
	}

	public void setAccount(AccountEntity account) {
		this.account = account;
	}

	@Override
	public EnumFavouriteType getType() {
		return EnumFavouriteType.ACCOUNT;
	}

}
