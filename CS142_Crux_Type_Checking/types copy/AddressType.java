package types;

public class AddressType extends Type {
    
    private Type base;
    
    public AddressType(Type base)
    {
        this.base = base;
    }
    
    public Type base()
    {
        return base;
    }

    @Override
    public String toString()
    {
        return "Address(" + base + ")";
    }

    @Override
    public boolean equivalent(Type that) {
        if (that == null)
            return false;
        if (!(that instanceof AddressType))
            return false;
        
        AddressType aType = (AddressType)that;
        return this.base.equivalent(aType.base);
    }

	@Override
    public Type assign(Type source) {
        if (source.equivalent(base)) 
         return base.assign(source);
         return super.assign(source);
	}

	@Override
	public Type deref() {
		return base();
	}

	/**
	 * Since we may have an address of any type, we need to implement the operators for 
	 * that specific Type.  The base type will handle any errors.
	 */

	@Override
    public Type not() {
        return base().not();
    }

	@Override
    public Type add(Type that) {
        return base().add(that);
    }

	@Override
    public Type sub(Type that) {
        return base().sub(that);
    }

	@Override
    public Type mul(Type that) {
        return base().mul(that);
    }

	@Override
    public Type div(Type that) {
        return base().div(that);
    }

	@Override
    public Type compare(Type that) {
        return base().compare(that);
    }

	@Override
    public Type index(Type index) {
        if (index.equivalent(new IntType()) && (base instanceof ArrayType)) {
			return new AddressType(base.index(index));
        } 

		return super.index(index);
    }

}

