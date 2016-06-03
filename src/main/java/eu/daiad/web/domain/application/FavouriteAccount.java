package eu.daiad.web.domain.application;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import eu.daiad.web.model.favourite.EnumFavouriteType;

@Entity(name = "favourite_account")
@Table(schema = "public", name = "favourite_account")
public class FavouriteAccount extends Favourite {

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "account_id", nullable = false)
	private Account account;

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	@Override
	public EnumFavouriteType getType() {
		return EnumFavouriteType.ACCOUNT;
	}

}
