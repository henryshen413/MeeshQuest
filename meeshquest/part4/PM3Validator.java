package cmsc420.meeshquest.part4;

public final class PM3Validator implements Validator {

	@Override
	public boolean ifValid(PMBlackNode b) {
		return b.getGeomList().size() + b.getAirportList().size() <= 1;
	}

	@Override
	public boolean ifValid(PMWhiteNode w) {
		return true;
	}

}
