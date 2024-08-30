This Java service is meant to read file based messages (regexed on suffix JSON for first iteration) and transmit them on various middleware interfaces that services connect to
This is meant as a generic utility for simulating messages coming from external sources . The idea with this code is to use as much core java as possible, except for say connection factories to middleware.

Later will use this in contrast to "library rich" code such as Camel based or latest Spring Framework enhancements
