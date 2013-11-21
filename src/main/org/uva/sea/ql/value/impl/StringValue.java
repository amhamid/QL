package org.uva.sea.ql.value.impl;

import org.uva.sea.ql.type.Type;
import org.uva.sea.ql.type.impl.StringType;
import org.uva.sea.ql.value.Value;

public class StringValue extends Value implements Comparable<StringValue>
{
	private String value;
	
	public StringValue(final String value)
	{
		this.value = value;
	}

    public String getValue()
    {
        return value;
    }

    @Override
    public Type getType()
    {
        return new StringType();
    }

    @Override
    public int compareTo(final StringValue o)
    {
        return this.value.compareTo(o.getValue());
    }

    @Override
    public boolean equals(final Object o)
    {
        if(this == o)
        {
            return true;
        }
        if(o == null || getClass() != o.getClass())
        {
            return false;
        }

        StringValue that = (StringValue) o;
        return !(value != null ? !value.equals(that.value) : that.value != null);
    }

    @Override
    public int hashCode()
    {
        return value != null ? value.hashCode() : 0;
    }

    @Override
    public String toString()
    {
        return value;
    }
}
