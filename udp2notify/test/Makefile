NAME = test
CXX = gcc
LINK = gcc
INCLUDES = `pkg-config --cflags glib-2.0 gtk+-2.0`
LIBS = `pkg-config --libs glib-2.0 gtk+-2.0` -lnotify
CFLAGS = -O2 -Wall -g

all: $(NAME).o
	$(LINK) $(CFLAGS) $(INCLUDES) -o $(NAME) $(NAME).o $(LIBS)
	

%.o: %.c
	$(CXX) $(CFLAGS) $(INCLUDES) -c $< $(NAME).c $(LIBS)

clean:
	rm $(NAME).o $(NAME)
