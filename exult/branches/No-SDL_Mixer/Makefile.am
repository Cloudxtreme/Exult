INCLUDES = -I$(srcdir) -I$(srcdir)/headers -I$(srcdir)/imagewin -I$(srcdir)/shapes \
	-I$(srcdir)/server -I$(srcdir)/data -I$(srcdir)/gamemgr \
	-I$(srcdir)/objs -I$(srcdir)/conf -I$(srcdir)/files -I$(srcdir)/gumps \
	-I$(srcdir)/audio -I$(srcdir)/audio/midi_drivers -I$(srcdir)/pathfinder \
	-I$(srcdir)/usecode \
	$(SDL_CFLAGS) $(INCDIRS) $(WINDOWING_SYSTEM) \
	$(DEBUG_FLAGS) $(CPPFLAGS) -DEXULT_DATADIR=\"$(EXULT_DATADIR)\"

CXXLINK = $(LIBTOOL) --mode=link $(CXXLD) $(AM_CXXFLAGS) $(DEBUG_FLAGS) $(CXXFLAGS) $(LDFLAGS) -o $(EXE_TARGET)

SUBDIRS =  files conf data shapes imagewin flic pathfinder \
	usecode tools audio gumps objs server gamemgr \
	mapedit desktop docs

bin_PROGRAMS = exult

exult_SOURCES =	\
	actions.cc	\
	actions.h	\
	actorio.cc	\
	actors.cc	\
	actors.h	\
	alpha_kludges.h	\
	args.cc		\
	args.h		\
	browser.cc	\
	browser.h	\
	cheat.cc	\
	cheat.h		\
	cheat_screen.cc	\
	cheat_screen.h	\
	combat.cc	\
	combat.h	\
	combat_opts.h	\
	delobjs.cc	\
	delobjs.h	\
	dir.cc		\
	dir.h		\
	drag.cc		\
	drag.h		\
	effects.cc	\
	effects.h	\
	exult.cc	\
	exult.h		\
	exulticon.h	\
	exultmenu.cc	\
	exultmenu.h	\
	exult_types.h	\
	exult_constants.h	\
	fnames.h	\
	frameseq.h	\
	game.cc		\
	game.h		\
	gameclk.cc	\
	gameclk.h	\
	gamedat.cc	\
	gamemap.cc	\
	gamemap.h	\
	gamerend.cc	\
	gamerend.h	\
	gamewin.cc	\
	gamewin.h	\
	hash_utils.h	\
	istring.cc	\
	istring.h	\
	keys.cc		\
	keys.h		\
	keyactions.cc	\
	keyactions.h	\
	menulist.cc	\
	menulist.h	\
	monsters.cc	\
	monsters.h	\
	mouse.cc	\
	mouse.h		\
	npcnear.cc	\
	npcnear.h	\
	npctime.cc	\
	npctime.h	\
	palette.cc	\
	palette.h	\
	party.cc	\
	party.h		\
	paths.cc	\
	paths.h		\
	readnpcs.cc	\
	ready.h		\
	rect.h		\
	schedule.cc	\
	schedule.h	\
	shapeid.cc	\
	shapeid.h	\
	singles.h	\
	tiles.h		\
	tqueue.cc	\
	tqueue.h	\
	txtscroll.cc	\
	txtscroll.h	\
	version.cc	\
	version.h	\
	xdrag.cc	\
	xdrag.h

EXULTLIBS = \
	pathfinder/libpathfinder.la		\
	audio/libaudio.la	\
	audio/midi_drivers/libmididrv.la	\
	audio/midi_drivers/timidity/libtimidity.la	\
	audio/midi_drivers/mt32emu/libmt32emu.la	\
	flic/libflic.la		\
	conf/libconf.la		\
	imagewin/libimagewin.la	\
	server/libserver.la \
	usecode/libusecode.la	\
	shapes/libshapes.la \
	objs/libobjs.la \
	gumps/libgumps.la \
	files/libu7file.la \
	files/zip/libminizip.la \
	gamemgr/libgamemgr.la

exult_LDADD = \
	$(EXULTLIBS) \
	$(SDL_LIBS) $(KMID_LIBS) $(SYSLIBS) $(x_libraries) $(ICON_FILE) \
	$(ZLIB_LIBS) $(OPENGL_LIBS)

exult_DEPENDENCIES = $(ICON_FILE) $(EXULTLIBS)

noinst_HEADERS = \
	alpha_kludges.h

EXTRA_DIST = 	\
	autogen.sh \
	README.1ST \
	README \
	FAQ \
	README.win32 \
	README.axp \
	README.BeOS \
	README.MacOS \
	README.MacOSX \
	README.AmigaOS \
	README.MorphOS \
	README.Qtopia \
	Makefile.be \
	Makefile.common \
	Makefile.mingw \
	Makefile.studio.mingw \
	Makefile.MorphOS \
	Info.plist.in \
	exult.spec \
	windrag.cc	\
	windrag.h \
	win32/exult.ico \
	win32/exultico.rc \
	win32/exultstudio.ico \
	win32/exultstudioico.rc \
	beos/exult.rsrc \
	mac/exult_icon.rsrc.bin \
	mac/ExultMac.mcp.xml.sit \
	mac/exult_version.rsrc.bin \
	mac/mac_autoconfig.h \
	mac/mac_prefix.h \
	macosx/exult.icns \
	msvcstuff/expack.vcproj \
	msvcstuff/Exult.vcproj \
	msvcstuff/Exult.sln \
	msvcstuff/Exult_bg_flx.vcproj \
	msvcstuff/Exult_flx.vcproj \
	msvcstuff/Exult_si_flx.vcproj \
	msvcstuff/mklink.vcproj \
	msvcstuff/msvc_kludges.cc \
	msvcstuff/msvc_kludges.h \
	msvcstuff/rip.vcproj \
	msvcstuff/shp2pcx.vcproj \
	msvcstuff/splitshp.vcproj \
	msvcstuff/ucdump.vcproj \
	msvcstuff/unistd.h \
	msvcstuff/wuc.vcproj \
	msvcstuff/wud.vcproj \
	msvcstuff/Zip.vcproj \
	msvcstuff/exconfig/exconfig.cpp \
	msvcstuff/exconfig/exconfig.def \
	msvcstuff/exconfig/exconfig.vcproj \
	msvcstuff/exconfig/exconfig.h \
	msvcstuff/exconfig/exconfig.rc \
	msvcstuff/exconfig/resource.h \
	msvcstuff/exconfig/StdAfx.cpp \
	msvcstuff/exconfig/StdAfx.h \
	msvc9/expack.vcproj \
	msvc9/Exult.vcproj \
	msvc9/Exult.sln \
	msvc9/Exult_bg_flx.vcproj \
	msvc9/Exult_flx.vcproj \
	msvc9/Exult_si_flx.vcproj \
	msvc9/mklink.vcproj \
	msvc9/msvc_kludges.cc \
	msvc9/msvc_kludges.h \
	msvc9/rip.vcproj \
	msvc9/shp2pcx.vcproj \
	msvc9/splitshp.vcproj \
	msvc9/ucdump.vcproj \
	msvc9/unistd.h \
	msvc9/wuc.vcproj \
	msvc9/wud.vcproj \
	msvc9/Zip.vcproj \
	msvc9/exconfig/exconfig.cpp \
	msvc9/exconfig/exconfig.def \
	msvc9/exconfig/exconfig.vcproj \
	msvc9/exconfig/exconfig.h \
	msvc9/exconfig/exconfig.rc \
	msvc9/exconfig/resource.h \
	msvc9/exconfig/StdAfx.cpp \
	msvc9/exconfig/StdAfx.h \
	headers/common_types.h \
	headers/exceptions.h \
	headers/gamma.h	\
	headers/pent_include.h \
	server/servewin32.cc \
	server/servewin32.h

win32/exultico.o: $(srcdir)/win32/exultico.rc $(srcdir)/win32/exult.ico
	-mkdir win32
	cp $(srcdir)/win32/exult.ico win32
	windres $(srcdir)/win32/exultico.rc win32/exultico.o

CLEANFILES = *~

if MACOSX

bundle_name = Exult.app

bundle: ${EXE_TARGET}
	mkdir -p $(bundle_name)/Contents/MacOS
	mkdir -p $(bundle_name)/Contents/Resources/data/
	echo "APPL????" > $(bundle_name)/Contents/PkgInfo
	$(INSTALL_DATA) Info.plist $(bundle_name)/Contents/
	$(INSTALL_PROGRAM) $< $(bundle_name)/Contents/MacOS/
	$(INSTALL_DATA) macosx/exult.icns $(bundle_name)/Contents/Resources/
	$(INSTALL_DATA) data/exult.flx $(bundle_name)/Contents/Resources/data
	$(INSTALL_DATA) data/exult_bg.flx $(bundle_name)/Contents/Resources/data
	$(INSTALL_DATA) data/exult_si.flx $(bundle_name)/Contents/Resources/data
	$(INSTALL_DATA) data/midisfx.flx $(bundle_name)/Contents/Resources/data
	$(INSTALL_DATA) data/exultmsg.txt $(bundle_name)/Contents/Resources/data

install-exec-local: bundle
	mkdir -p $(DESTDIR)/Applications/
	cp -r $(bundle_name) $(DESTDIR)/Applications/

else
install-exec-local:

endif