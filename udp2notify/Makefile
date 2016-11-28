NAME = udp2notify
CXX = g++
LINK = g++
INCLUDES = `pkg-config --cflags glib-2.0 gtk+-2.0`
LIBS = `pkg-config --libs glib-2.0 gtk+-2.0` -lnotify
CFLAGS = -O2 -Wall

all: $(NAME).o
	$(LINK) $(INCLUDES) -o $(NAME) $(NAME).o $(LIBS)
	

%.o: %.cpp
	$(CXX) $(INCLUDES) -c $< $(NAME).cpp $(LIBS)

clean:
	rm $(NAME).o $(NAME)
