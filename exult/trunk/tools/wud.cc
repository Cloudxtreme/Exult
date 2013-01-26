#include <fstream>
#include <stdlib.h>
#include <string.h>
#include "uctools.h"
#include "utils.h"
#include "ucsymtbl.h"

using std::ifstream;
using std::istream;
using std::ios;

Usecode_symbol_table *symtbl = 0;

// Prints module's data segment
void printdataseg(istream &in, unsigned int ds) {
	long pos;
	unsigned int off = 0;
	char *p;
	char *pp;
	char tempstr[61], *tempstr2;
	pos = in.tellg();
	pp = p = new char[ds];
	in.read(p, ds);
	in.seekg(pos);
	printf("\t\t.data\n");
	while (off < ds) {
		int len;
		unsigned int localoff = 0;
		while ((len = (strlen(pp) > 60) ? 60 : strlen(pp))) {
			memcpy(tempstr, pp, len);
			tempstr[len] = '\0';
			if (!localoff)
				printf("L%04X:", off);
			while (strchr(tempstr, 13)) {
				tempstr2 = strchr(tempstr, 13) + 2;
				tempstr[strchr(tempstr, 13) - tempstr] = 0;
				printf("\tdb\t\'%s\'\n\tdb\t0d\n\tdb\t0a\n", tempstr);
				strcpy(tempstr, tempstr2);
			}
			printf("\tdb\t\'%s\'\n", tempstr);
			localoff += len;
			pp += len;
		}
		pp++;
		printf("L%04X:\tdb\t00\n", off + localoff);
		off += localoff + 1;
	}
	delete p;
}

// Prints single opcode
// Return number of bytes to advance the code pointer
// Prints first characters of strings referenced
unsigned int print_opcode(unsigned char *ptrc, unsigned int coffset,
                          unsigned char *pdataseg, unsigned short *pextern,
                          unsigned int externsize, const char **func_table, int funsize) {
	unsigned int nbytes;
	unsigned int i;
	unsigned int opcode = Read1(ptrc);
	// Find the description
	opcode_desc *pdesc = (opcode >= (sizeof(opcode_table) / sizeof(opcode_desc))) ?
	                     NULL : opcode_table + opcode;
	if (pdesc && (pdesc->mnemonic == NULL)) {
		printf("Unknown opcode: %x\n", opcode);
		// Unknown opcode
		pdesc = NULL;
	}
	// Number of bytes to print
	nbytes = pdesc ? (pdesc->nbytes + 1) : 1;
	// Print label
	printf("%04X: %02X ", coffset, opcode);
	// Print bytes
	for (i = 0; i < nbytes - 1; i++)
		printf("%02X ", ptrc[i]);
	// Print mnemonic
	if (nbytes < 4)
		printf("\t");
	if (pdesc)
		printf("\t%s", pdesc->mnemonic);
	else
		printf("\tdb %x\t???", opcode);
	// Print operands if any
	if ((nbytes == 1) || (pdesc == NULL && pdesc->type == 0)) {
		printf("\n");
		return nbytes;
	}
	switch (pdesc->type) {
	case op_byte:
		printf("\t%x\n", Read1(ptrc));
		break;
	case op_push:
		for (i = 1; i < nbytes; i++)
			printf("\n%04X:\t\t\tdb %x", coffset + i, Read1(ptrc));
		printf("\n");
		break;
	case op_immed:
	case op_argnum:
	case op_funid:
	case op_clsfun:
	case op_clsid: {
		int immed = Read2(ptrc);
		// Print immediate operand
		printf("\t%04XH\t\t\t; %d\n", immed, static_cast<short>(immed));
		break;
	}
	case op_immed32: {
		int immed = Read4(ptrc);
		printf("\t%04XH\t\t\t; %d\n", immed, static_cast<int>(immed));
		break;
	}
	case op_data_string:
	case op_data_string32: {
		unsigned int ostr;
		if (pdesc->type == op_data_string)
			ostr = Read2(ptrc);
		else
			ostr = Read4(ptrc);
		// Print data string operand
		char const *pstr = reinterpret_cast<char *>(pdataseg) + ostr;

		unsigned int len = strlen(pstr);
		unsigned int minlen = len > 20 ? 20 - 3 : len;

		printf("\tL%04X\t\t\t; ", ostr);
		for (i = 0; i < minlen; i++)
			printf("%c", pstr[i]);
		if (minlen < len)
			// String truncated
			printf("...");
		printf("\n");
	}
	break;
	case op_relative_jump:
	case op_unconditional_jump:
		ptrc += nbytes - 2 - 1;
		// Print jump desination
		printf("\t%04X\n", Read2s(ptrc) + coffset + nbytes);
// debugging printf("nbytes=%d, coffset=%d\n", nbytes, coffset);
		break;
	case op_relative_jump32:
	case op_uncond_jump32:
		ptrc += nbytes - 4 - 1;
		printf("\t%04X\n", Read4s(ptrc) + coffset + nbytes);
		break;
	case op_sloop:  /* WJP */
	case op_static_sloop: {
		ptrc += nbytes - 10 - 1;
		unsigned short immed1 = Read2(ptrc), immed2 = Read2(ptrc),
		               immed3 = Read2(ptrc), immed4 = Read2(ptrc);
		printf("\t[%04X], [%04X], [%04X], [%04X], %04X\n",
		       immed1, immed2, immed3, immed4,
		       Read2s(ptrc) + coffset + nbytes);
		break;
	}
	case op_sloop32: { /* WJP */
		ptrc += nbytes - 12 - 1;
		unsigned short immed1 = Read2(ptrc), immed2 = Read2(ptrc),
		               immed3 = Read2(ptrc), immed4 = Read2(ptrc);
		printf("\t[%04X], [%04X], [%04X], [%04X], %04X\n",
		       immed1, immed2, immed3, immed4,
		       Read4s(ptrc) + coffset + nbytes);
		break;
	}
	case op_immed_and_relative_jump:    /* JSF */
	case op_argnum_reljump: {
		int immed1 = Read2(ptrc), immed2 = Read2s(ptrc);
		printf("\t%04XH, %04X\n", immed1, immed2 + coffset + 5);
		break;
	}
	case op_immedreljump32:
	case op_argnum_reljump32: {
		int immed1 = Read2(ptrc), immed2 = Read4s(ptrc);
		printf("\t%04XH, %04X\n", immed1, immed2 + coffset + 5);
		break;
	}
	case op_call: {
		// Print call operand
		unsigned short func = Read2(ptrc);
		unsigned char nparms = Read1(ptrc);
		if ((func < funsize) && func_table[func]) {
			// Known function
			Usecode_symbol *fsym = symtbl ? (*symtbl)[func] : 0;
			if (fsym)
				printf("\t_%s@%d (%04X)\t\t\t; %s\n",
				       func_table[func], nparms, func,
				       fsym->get_name());
			else
				printf("\t_%s@%d (%04X)\n",
				       func_table[func], nparms, func);
		} else
			// Unknown function
			printf("\t%04X, %d\n", func, nparms);
	}
	break;
	case op_extcall: {
		// Print extern call
		unsigned short externpos = Read2(ptrc);
		if (externpos < externsize) {
			unsigned short func = pextern[externpos];
			Usecode_symbol *fsym = symtbl ? (*symtbl)[func] : 0;
			if (fsym)
				printf("\t[%04X]\t\t\t; %s\n",
				       externpos, fsym->get_name());
			else
				printf("\t[%04X]\t\t\t; %04XH\n",
				       externpos, func);
		} else
			printf("\t[%04X]\t\t\t; ????\n", externpos);
	}
	break;
	case op_varref:
	case op_staticref:
	case op_classvarref:
		// Print variable reference
		printf("\t[%04X]\n", Read2(ptrc));
		break;
	case op_immed_pair:
	case op_clsfun_vtbl: {
		int immed1 = Read2(ptrc), immed2 = Read2(ptrc);
		printf("\t%04XH, %04XH\n", immed1, immed2);
		break;
	}
	case op_flgref:
		// Print global flag reference
		printf("\tflag:[%04X]\n", Read2(ptrc));
		break;
	default:
		// Unknown type
		printf("\n");
		break;
	}
	return nbytes;
}

void printcodeseg(istream &in, unsigned int ds, unsigned int s,
                  const char **func_table, int funsize, int extended) {
	long pos;
	unsigned int size;
	unsigned int externsize;
	unsigned int i;
	unsigned int offset;
	unsigned int nbytes;
	char *p;
	unsigned char *pp;
	char *pdata;
	unsigned short *pextern;
	pos = in.tellg();
	if (extended == 0) {
		size = s - ds - sizeof(unsigned short);
	} else {
		size = s - ds - sizeof(unsigned int);
	}

	p = new char[size];
	pp = reinterpret_cast<unsigned char *>(p);
	pdata = new char[ds];
	in.read(pdata, ds);
	printf("\t\t.code\n");
	in.read(p, size);
	in.seekg(pos);
	// Print code segment header
	if (size < 3 * sizeof(unsigned short)) {
		printf("Code segment bad!\n");
		delete [] p;
		delete [] pdata;
		return;
	}
	// Print argument counter
	printf("\t\t.argc %04XH\n", Read2(pp));
	// Print locals counter
	printf("\t\t.localc %04XH\n", Read2(pp));
	// Print externs section
	externsize = Read2(pp);
	printf("\t\t.externsize %04XH\n", externsize);
	if (size < ((3 + externsize) * sizeof(unsigned short))) {
		printf("Code segment bad!\n");
		delete [] p;
		delete [] pdata;
		return;
	}
	size -= ((3 + externsize) * sizeof(unsigned short));
	pextern = reinterpret_cast<unsigned short *>(pp);
	for (i = 0; i < externsize; i++)
		printf("\t\t.extern %04XH\n", Read2(pp));
	offset = 0;
	// Print opcodes
	while (offset < size) {
		nbytes = print_opcode(pp, offset,
		                      reinterpret_cast<unsigned char *>(pdata), pextern, externsize,
		                      func_table, funsize);
		pp += nbytes;
		offset += nbytes;
	}
	delete [] p;
	delete [] pdata;
}

/*
 *  Note:  func = -1 just to print header, funcnum to print it all, or
 *      -2 for any function.
 */
void printfunc(istream &in, long func, int i, const char **func_table, int funsize) {
	unsigned short funcnum;
	unsigned int s, ds;
	long off, bodyoff;
	int extended = 0;
	// Save start offset
	off = in.tellg();
	// Read function header
	funcnum = Read2(in);

	if (funcnum == 0xFFFF) {
		funcnum = Read2(in);
		s = Read4(in);
		bodyoff = in.tellg();
		ds = Read4(in);
		extended = 1;
	} else {
		s = Read2(in);
		// Save body offset
		bodyoff = in.tellg();
		ds = Read2(in);
	}

	if (func == -1)
		printf("\tFunction #%d (%04XH), offset = %08lx, size = %04x, data = %04x\n", i,
		       funcnum, off, s, ds);
	if (funcnum == func || func == -2) {
		Usecode_symbol *fsym = symtbl ? (*symtbl)[funcnum] : 0;
		if (fsym)
			printf("\t\t.funcnumber\t%04XH\t\t\t; %s\n", funcnum,
			       fsym->get_name());
		else
			printf("\t\t.funcnumber\t%04XH\n", funcnum);
		if (extended == 1)
			printf("\t\t.ext32\n");
		// Dump function contents
		printdataseg(in, ds);
		printcodeseg(in, ds, s, func_table, funsize, extended);
	}
	// Seek back, then to next function
	in.seekg(bodyoff);
	in.seekg(s, ios::cur);
}

int main(int argc, char **argv) {
	long func = -1;
	long sz;
	int i = 0;
	ifstream in;
	int findex = 1;         // Argv index of file.
	const char **func_table = bg_intrinsic_table;
	int funsize = bg_intrinsic_size;


	if (argc < 2) {
		fprintf(stderr, "Usage\n%s [-s] usecode_file [func_num]\n", argv[0]);
		return -1;
	}
	// Serpent Isle?
	if (strcmp(argv[1], "-s") == 0) {
		findex++;
		func_table = si_intrinsic_table;
		funsize = si_intrinsic_size;
	}
	if (!U7open(in, argv[findex])) {
		fprintf(stderr, "Failed to open %s\n\n", argv[findex]);
		return 0;
	}
	in.seekg(0, ios::end);
	sz = in.tellg();
	in.seekg(0);
	long magic = Read4(in);     // Test for symbol table.
	if (magic == UCSYMTBL_MAGIC0 && (magic = Read4(in))
	        == UCSYMTBL_MAGIC1) {
		if (!symtbl)
			symtbl = new Usecode_symbol_table();
		symtbl->read(in);
	} else
		in.seekg(0);

	if (argc > findex + 1) {
		char *stopstr;
		if (strcmp(argv[findex + 1], "-a") == 0)
			func = -2;  // Print ALL functions.
		else
			func = strtoul(argv[findex + 1], &stopstr, 16);
	}
	while (in.tellg() < sz) {
		printfunc(in, func, i, func_table, funsize);
		i++;
	}
	if (func == -1) {
		long pos = in.tellg();
		if (pos != sz)
			fprintf(stderr, "Problem, tell = %ld!\n", pos);
		printf("Functions: %d\n", i);
	}
	in.close();
	return 0;
}
