package com.atlassian.gadgets.opensocial.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.jcip.annotations.Immutable;

import static java.util.Collections.unmodifiableList;

/**
 * Immutable container class encapsulating social data for a person. Person objects have a unique identifier,
 * which is used for equality and hashing.
 *
 * @since 2.0
 */
@Immutable
public final class Person
{
    private PersonId personId;
    private Name name;
    private URI profileUrl;
    private URI thumbnailUrl;
    private List<Address> addresses;
    private List<PhoneNumber> phoneNumbers;
    private List<EmailAddress> emailAddresses;
    private String aboutMe;
    private String status;
    private Date dateOfBirth;
    private int timeZone;
    private Address currentLocation;
    private List<URI> urls;

    private Person(Person.Builder builder)
    {
        this.personId = builder.personId;
        this.name = builder.name;
        this.profileUrl = builder.profileUrl;
        this.thumbnailUrl = builder.thumbnailUrl;
        this.addresses = builder.addresses;
        this.phoneNumbers = builder.phoneNumbers;
        this.emailAddresses = builder.emailAddresses;
        this.aboutMe = builder.aboutMe;
        this.status = builder.status;
        this.dateOfBirth = builder.dateOfBirth;
        this.timeZone = builder.timeZone;
        this.currentLocation = builder.currentLocation;
        this.urls = builder.urls;
    }

    /**
     * A (unique) username/id for this person
     * @return the unique username/id for this person
     */
    public PersonId getPersonId()
    {
        return personId;
    }

    /**
     * The full name for this person
     * @return a {@code Name} representing the person's full name
     */
    public Name getName()
    {
        return name;
    }

    /**
     * An "about me" blurb
     * @return text displayed fro the "about me" section of the {@code Person}'s profile
     */
    public String getAboutMe()
    {
        return aboutMe;
    }

    /**
     * A list of the person's addresses
     * @return the person's addresses
     */
    public List<Address> getAddresses()
    {
        return addresses;
    }

    /**
     * @return the person's current location, as an {@code Address}
     */
    public Address getCurrentLocation()
    {
        return currentLocation;
    }

    /**
     * @return the person's date of birth
     */
    public Date getDateOfBirth()
    {
        return dateOfBirth;
    }

    /**
     * @return email addresses associated with the person
     */
    public List<EmailAddress> getEmailAddresses()
    {
        return emailAddresses;
    }

    /**
     * @return a list of phone numbers associated with the person
     */
    public List<PhoneNumber> getPhoneNumbers()
    {
        return phoneNumbers;
    }

    /**
     * @return a url for the person's profile
     */
    public URI getProfileUrl()
    {
        return profileUrl;
    }

    /**
     * @return the person's status text
     */
    public String getStatus()
    {
        return status;
    }

    /**
     * @return a url for the person's thumbnail
     */
    public URI getThumbnailUrl()
    {
        return thumbnailUrl;
    }

    /**
     * @return the timezone offset, specified as the number of minutes from GMT
     */
    public int getTimeZone()
    {
        return timeZone;
    }

    /**
     * @return a list of the person's interesting urls
     */
    public List<URI> getUrls()
    {
        return urls;
    }

    public boolean equals(Object obj)
    {
        return obj instanceof Person && personId.equals(((Person)obj).getPersonId());
    }

    public int hashCode()
    {
        return personId.hashCode();
    }

    public String toString()
    {
        return personId.toString();
    }

    /**
     * A builder that facilitates construction of {@link Person} objects. The final {@link Person} is returned by the
     * {@code build} method
     */
    public static final class Builder
    {
        private PersonId personId;
        private Name name;
        private URI profileUrl;
        private URI thumbnailUrl;
        private List<Address> addresses;
        private List<PhoneNumber> phoneNumbers;
        private List<EmailAddress> emailAddresses;
        private String aboutMe;
        private String status;
        private Date dateOfBirth;
        private int timeZone;
        private Address currentLocation;
        private List<URI> urls;

        /**
         * Create a builder initialized with the values from the specified person argument
         * @param person the person from which this builder's values should be initialized
         */
        public Builder(Person person)
        {
            this.personId = person.personId;
            this.name = person.name;
            this.profileUrl = person.profileUrl;
            this.thumbnailUrl = person.thumbnailUrl;
            this.addresses = unmodifiableList(new ArrayList<Address>(person.addresses));
            this.phoneNumbers = unmodifiableList(new ArrayList<PhoneNumber>(person.phoneNumbers));
            this.emailAddresses = unmodifiableList(new ArrayList<EmailAddress>(person.emailAddresses));
            this.aboutMe = person.aboutMe;
            this.status = person.status;
            this.dateOfBirth = new Date(person.dateOfBirth.getTime());
            this.timeZone = person.timeZone;
            this.currentLocation = person.currentLocation;
            this.urls = unmodifiableList(person.urls);
        }

        /**
         * Create a builder with the given the {@code PersonId} for the {@code Person} to be constructed
         * @param personId the unique ID of the person being constructed
         */
        public Builder (PersonId personId)
        {
            this.personId = personId;
        }

        /**
         * Set the {@code Name} of the {@code Person} being constructed
         * @param name the full name of the person being constructed
         * @return the builder for futher construction
         */
        public Builder name(Name name)
        {
            this.name = name;
            return this;
        }

        /**
         * Set the profile URI of the {@code Person} being constructed
         * @param uri the profile URI to set
         * @return the builder for further construction
         */
        public Builder profileUri(URI uri)
        {
            this.profileUrl = uri;
            return this;
        }

        /**
         * Set the thumbnail URI of the {@code Person} being constructed
         * @param uri the thumbnail URI to set
         * @return the builder for further construction
         */
        public Builder thumbnailUri(URI uri)
        {
            this.thumbnailUrl = uri;
            return this;
        }

        /**
         * Set the list of addresses for the {@code Person} being constructed
         * @param addresses the addresses to set
         * @return the builder for further construction
         */
        public Builder addresses(List<Address> addresses)
        {
            this.addresses = unmodifiableList(new ArrayList<Address>(addresses));
            return this;
        }

        /**
         * Set the list of phone numbers for the {@code Person} being constructed
         * @param phoneNumbers the phone numbers to set
         * @return the builder for further construction
         */
        public Builder phoneNumbers(List<PhoneNumber> phoneNumbers)
        {
            this.phoneNumbers = unmodifiableList(new ArrayList<PhoneNumber>(phoneNumbers));
            return this;
        }

        /**
         * Sets the list of email addressees for the {@code Person} being constructed
         * @param emailAddresses the email addresses to set
         * @return the builder for further construction
         */
        public Builder emailAddresses(List<EmailAddress> emailAddresses)
        {
            this.emailAddresses = unmodifiableList(new ArrayList<EmailAddress>(emailAddresses));
            return this;
        }

        /**
         * Sets the value of the "about me" string for the {@code Person} being constructed
         * @param aboutMe the text to set for the "about me" blurb
         * @return the builder for further construction
         */
        public Builder aboutMe(String aboutMe)
        {
            this.aboutMe = aboutMe;
            return this;
        }

        /**
         * Sets the value of the person's "status" text
         * @param status the text to set for the person's status
         * @return the builder for further construction
         */
        public Builder status(String status)
        {
            this.status = status;
            return this;
        }

        /**
         * Sets the person's date of birth
         * @param date the date of birth
         * @return the builder for further construction
         */
        public Builder dateOfBirth(Date date)
        {
            this.dateOfBirth = new Date(date.getTime());
            return this;
        }

        /**
         * Sets the time zone offset for the person. The offset is specified as the difference in minutes from GMT
         * 
         * @param timeZone the time zone offset for the person.
         * @return the builder for further construction
         */
        public Builder timeZone(int timeZone)
        {
            this.timeZone = timeZone;
            return this;
        }

        /**
         * Sets the current location for the person.
         * @param currentLocation the location to set, as an {@code Address}
         * @return the builder for further construction
         */
        public Builder currentLocation(Address currentLocation)
        {
            this.currentLocation = currentLocation;
            return this;
        }

        /**
         * Sets a list of interesting URLs for the person
         * @param urls the URLs for the person
         * @return the builder for further construction
         */
        public Builder urls(List<URI> urls)
        {
            this.urls = unmodifiableList(new ArrayList<URI>(urls));
            return this;
        }

        /**
         * Build the {@code Person}
         * @return the constructed {@code Person}
         */
        public Person build()
        {
            return new Person(this);
        }
    }
}